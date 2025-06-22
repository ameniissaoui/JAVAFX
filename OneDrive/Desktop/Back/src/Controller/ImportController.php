<?php
// src/Controller/ImportController.php
namespace App\Controller;

use App\Service\ExcelImportService;
use App\Repository\EmployeeRepository;
use App\Repository\MonthlyActivityRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/import")
 */
class ImportController extends AbstractController
{
    private ExcelImportService $importService;
    private EmployeeRepository $employeeRepository;
    private MonthlyActivityRepository $activityRepository;

    public function __construct(
        ExcelImportService $importService,
        EmployeeRepository $employeeRepository,
        MonthlyActivityRepository $activityRepository
    ) {
        $this->importService = $importService;
        $this->employeeRepository = $employeeRepository;
        $this->activityRepository = $activityRepository;
    }

    /**
     * @Route("/upload", name="import_upload", methods={"POST"})
     */
    public function upload(Request $request): Response
    {
        $uploadedFile = $request->files->get('excel_file');

        if (!$uploadedFile) {
            $this->addFlash('error', 'Aucun fichier sélectionné');
            return $this->redirectToRoute('import_index');
        }

        try {
            // Déplacer le fichier temporairement
            $fileName = uniqid() . '.' . $uploadedFile->getClientOriginalExtension();
            $uploadedFile->move($this->getParameter('kernel.project_dir') . '/var/uploads', $fileName);
            $filePath = $this->getParameter('kernel.project_dir') . '/var/uploads/' . $fileName;

            // Importer les données
            $results = $this->importService->importFromExcel($filePath);

            // Supprimer le fichier temporaire
            unlink($filePath);

            // Récupérer les données importées pour l'affichage
            $importedData = $this->getImportedDataForDisplay();

            return $this->render('import/results.html.twig', [
                'results' => $results,
                'imported_data' => $importedData
            ]);

        } catch (\Exception $e) {
            $this->addFlash('error', 'Erreur lors de l\'import: ' . $e->getMessage());
            return $this->redirectToRoute('import_index');
        }
    }

    private function getImportedDataForDisplay(): array
    {
        $employees = $this->employeeRepository->findAll();
        $importedData = [];

        foreach ($employees as $employee) {
            $activities = $this->activityRepository->findByEmployee($employee);
            
            if (!empty($activities)) {
                $importedData[] = [
                    'code' => $employee->getEmployeeCode(),
                    'activities' => $activities
                ];
            }
        }

        return $importedData;
    }
}