<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Form\RegistrationFormType;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use App\Repository\MonthlyActivityRepository;
use App\Repository\EmployeeRepository;
use App\Service\ExcelImportService;

final class BackController extends AbstractController
{
    #[Route('/dashboard', name: 'app_back')]
    public function index(): Response
    {
        return $this->render('back/index.html.twig', [
            'controller_name' => 'BackController',
        ]);
    }

    #[Route('/table', name: 'app_table')]
    public function table(MonthlyActivityRepository $activityRepository): Response
    {
        // Récupérer toutes les activités depuis la base de données
        $activities = $activityRepository->findAll();
        
        // Organiser les données pour le template
        $imported_data = [];
        $employees_data = [];
        
        foreach ($activities as $activity) {
            $employeeCode = $activity->getEmployee()->getEmployeeCode();
            
            if (!isset($employees_data[$employeeCode])) {
                $employees_data[$employeeCode] = [
                    'code' => $employeeCode,
                    'activities' => []
                ];
            }
            
            $employees_data[$employeeCode]['activities'][] = $activity;
        }
        
        $imported_data = array_values($employees_data);
        
        // Statistiques pour l'affichage
        $results = [
            'employees_created' => count($employees_data),
            'activities_created' => count($activities),
            'activities_updated' => 0,
            'errors' => []
        ];

        return $this->render('back/tables.html.twig', [
            'controller_name' => 'BackController',
            'imported_data' => $imported_data,
            'results' => $results
        ]);
    }

    #[Route('/import-excel', name: 'app_import_excel', methods: ['POST'])]
    public function importExcel(
        Request $request, 
        ExcelImportService $importService,
        MonthlyActivityRepository $activityRepository
    ): Response {
        $uploadedFile = $request->files->get('excel_file');

        if (!$uploadedFile) {
            $this->addFlash('error', 'Aucun fichier sélectionné');
            return $this->redirectToRoute('app_table');
        }

        try {
            // Créer le dossier uploads s'il n'existe pas
            $uploadsDir = $this->getParameter('kernel.project_dir') . '/var/uploads';
            if (!is_dir($uploadsDir)) {
                mkdir($uploadsDir, 0755, true);
            }

            // Déplacer le fichier
            $fileName = uniqid() . '.' . $uploadedFile->getClientOriginalExtension();
            $uploadedFile->move($uploadsDir, $fileName);
            $filePath = $uploadsDir . '/' . $fileName;

            // Importer les données
            $results = $importService->importFromExcel($filePath);

            // Supprimer le fichier temporaire
            unlink($filePath);

            // Récupérer les données pour l'affichage
            $activities = $activityRepository->findAll();
            $employees_data = [];
            
            foreach ($activities as $activity) {
                $employeeCode = $activity->getEmployee()->getEmployeeCode();
                
                if (!isset($employees_data[$employeeCode])) {
                    $employees_data[$employeeCode] = [
                        'code' => $employeeCode,
                        'activities' => []
                    ];
                }
                
                $employees_data[$employeeCode]['activities'][] = $activity;
            }
            
            $imported_data = array_values($employees_data);

            $this->addFlash('success', sprintf(
                'Import réussi: %d employés, %d activités créées',
                $results['employees_created'],
                $results['activities_created']
            ));

            return $this->render('back/tables.html.twig', [
                'controller_name' => 'BackController',
                'imported_data' => $imported_data,
                'results' => $results
            ]);

        } catch (\Exception $e) {
            $this->addFlash('error', 'Erreur lors de l\'import: ' . $e->getMessage());
            return $this->redirectToRoute('app_table');
        }
    }

    #[Route('/charts', name: 'app_charts')]
    public function charts(): Response
    {
        return $this->render('back/charts.html.twig', [
            'controller_name' => 'BackController',
        ]);
    }

    #[Route('/profile', name: 'app_profile')]
    public function profile(): Response
    {
        return $this->render('back/profile.html.twig', [
            'controller_name' => 'BackController',
        ]);
    }

    #[Route('/register', name: 'app_register')]
    public function register(Request $request): Response
    {
        $form = $this->createForm(RegistrationFormType::class);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            return $this->redirectToRoute('app_login');
        }

        return $this->render('back/register.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        $error = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        return $this->render('back/login.html.twig', [
            'last_username' => $lastUsername,
            'error' => $error,
        ]);
    }
#[Route('/debug-excel', name: 'app_debug_excel', methods: ['POST'])]
public function debugExcel(Request $request): Response
{
    $uploadedFile = $request->files->get('excel_file');

    if (!$uploadedFile) {
        return new Response('Aucun fichier uploadé');
    }

    try {
        // Sauvegarder temporairement le fichier
        $uploadsDir = $this->getParameter('kernel.project_dir') . '/var/uploads';
        if (!is_dir($uploadsDir)) {
            mkdir($uploadsDir, 0755, true);
        }

        $fileName = 'debug_' . uniqid() . '.' . $uploadedFile->getClientOriginalExtension();
        $uploadedFile->move($uploadsDir, $fileName);
        $filePath = $uploadsDir . '/' . $fileName;

        // Lire le fichier Excel
        $spreadsheet = \PhpOffice\PhpSpreadsheet\IOFactory::load($filePath);
        $worksheet = $spreadsheet->getActiveSheet();
        
        $debugData = [];
        $rowIndex = 0;
        
        foreach ($worksheet->getRowIterator() as $row) {
            $rowIndex++;
            if ($rowIndex > 30) break; // Limiter à 30 lignes pour le debug
            
            $cellA = $worksheet->getCell('A' . $rowIndex)->getCalculatedValue();
            $cellB = $worksheet->getCell('B' . $rowIndex)->getCalculatedValue();
            $cellC = $worksheet->getCell('C' . $rowIndex)->getCalculatedValue();
            
            // Nettoyer les valeurs
            $cellA_clean = trim((string)$cellA);
            $cellB_clean = is_numeric($cellB) ? (float)$cellB : $cellB;
            $cellC_clean = is_numeric($cellC) ? (float)$cellC : $cellC;
            
            // Tests de détection
            $isEmployeeCode = preg_match('/^\d+[A-Z]{3}$/', $cellA_clean);
            $isMonthName = $this->isMonthNameDebug($cellA_clean);
            
            $debugData[] = [
                'row' => $rowIndex,
                'cellA_raw' => $cellA,
                'cellA_clean' => $cellA_clean,
                'cellB' => $cellB_clean,
                'cellC' => $cellC_clean,
                'is_employee_code' => $isEmployeeCode,
                'is_month_name' => $isMonthName,
                'cellA_length' => strlen($cellA_clean),
                'cellA_type' => gettype($cellA)
            ];
        }

        // Supprimer le fichier temporaire
        unlink($filePath);

        return $this->render('back/debug.html.twig', [
            'debug_data' => $debugData,
            'filename' => $uploadedFile->getClientOriginalName()
        ]);

    } catch (\Exception $e) {
        return new Response('Erreur: ' . $e->getMessage());
    }
}

private function isMonthNameDebug(string $value): bool
{
    $months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    
    foreach ($months as $month) {
        if (stripos($value, $month) !== false) {
            return true;
        }
    }
    
    return false;
}
}