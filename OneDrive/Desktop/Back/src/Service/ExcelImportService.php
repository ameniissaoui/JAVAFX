<?php
// src/Service/ExcelImportService.php
namespace App\Service;

use App\Entity\Employee;
use App\Entity\MonthlyActivity;
use App\Repository\EmployeeRepository;
use App\Repository\MonthlyActivityRepository;
use Doctrine\ORM\EntityManagerInterface;
use PhpOffice\PhpSpreadsheet\IOFactory;

class ExcelImportService
{
    private EntityManagerInterface $entityManager;
    private EmployeeRepository $employeeRepository;
    private MonthlyActivityRepository $activityRepository;

    public function __construct(
        EntityManagerInterface $entityManager,
        EmployeeRepository $employeeRepository,
        MonthlyActivityRepository $activityRepository
    ) {
        $this->entityManager = $entityManager;
        $this->employeeRepository = $employeeRepository;
        $this->activityRepository = $activityRepository;
    }

    public function importFromExcel(string $filePath): array
    {
        $spreadsheet = IOFactory::load($filePath);
        $worksheet = $spreadsheet->getActiveSheet();
        
        $results = [
            'employees_created' => 0,
            'activities_created' => 0,
            'activities_updated' => 0,
            'errors' => []
        ];

        $currentEmployee = null;
        $employeesProcessed = [];

        // Parcourir toutes les lignes
        foreach ($worksheet->getRowIterator() as $row) {
            $rowIndex = $row->getRowIndex();
            
            // Ignorer les 3 premières lignes (en-têtes)
            if ($rowIndex <= 3) {
                continue;
            }

            $cellA = trim((string)$worksheet->getCell('A' . $rowIndex)->getCalculatedValue());
            $cellB = $worksheet->getCell('B' . $rowIndex)->getCalculatedValue();
            $cellC = $worksheet->getCell('C' . $rowIndex)->getCalculatedValue();

            try {
                // Ligne avec code employé
                if ($this->isEmployeeCode($cellA)) {
                    $currentEmployee = $this->findOrCreateEmployee($cellA);
                    
                    // Compter seulement les nouveaux employés
                    if (!in_array($cellA, $employeesProcessed)) {
                        $employeesProcessed[] = $cellA;
                        $results['employees_created']++;
                    }
                }
                // Ligne avec données mensuelles
                elseif ($currentEmployee && $this->isMonthName($cellA)) {
                    // Vérifier que B et C ne sont pas vides/zéro
                    if (!empty($cellB) && !empty($cellC) && $cellB > 0 && $cellC > 0) {
                        $result = $this->createOrUpdateActivity(
                            $currentEmployee, 
                            $cellA, 
                            (float)$cellB, 
                            (float)$cellC
                        );
                        
                        if ($result['created']) {
                            $results['activities_created']++;
                        } else {
                            $results['activities_updated']++;
                        }
                    }
                }

            } catch (\Exception $e) {
                $results['errors'][] = [
                    'row' => $rowIndex,
                    'error' => $e->getMessage(),
                    'data' => ['A' => $cellA, 'B' => $cellB, 'C' => $cellC]
                ];
            }
        }

        // Sauvegarder tout
        $this->entityManager->flush();

        return $results;
    }

    private function isEmployeeCode(string $value): bool
    {
        // Améliorer la regex pour détecter les codes comme 6190473285BPI et 6510239748GRD
        return preg_match('/^\d{10,}[A-Z]{3}$/', $value);
    }

    private function isMonthName(string $value): bool
    {
        $months = [
            'January', 'February', 'March', 'April', 'May', 'June',
            'July', 'August', 'September', 'October', 'November', 'December'
        ];
        
        foreach ($months as $month) {
            if (stripos($value, $month) !== false && strpos($value, '2025') !== false) {
                return true;
            }
        }
        
        return false;
    }

    private function findOrCreateEmployee(string $code): Employee
    {
        $employee = $this->employeeRepository->findByCode($code);
        
        if (!$employee) {
            $employee = new Employee();
            $employee->setEmployeeCode($code);
            $this->entityManager->persist($employee);
        }
        
        return $employee;
    }

    private function createOrUpdateActivity(
        Employee $employee, 
        string $monthName, 
        float $daysWorked, 
        float $revenue
    ): array {
        // Chercher si l'activité existe déjà
        $activity = $this->activityRepository->findOneBy([
            'employee' => $employee,
            'monthName' => trim($monthName)
        ]);

        $created = false;
        if (!$activity) {
            $activity = new MonthlyActivity();
            $activity->setEmployee($employee);
            $activity->setMonthName(trim($monthName));
            $created = true;
        }

        // Mettre à jour les données
        $activity->setActualResourceUsage($daysWorked);
        $activity->setActualInternalRevenue($revenue);

        $this->entityManager->persist($activity);

        return ['created' => $created];
    }
}