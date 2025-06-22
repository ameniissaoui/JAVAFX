<?php

namespace App\Entity;

use App\Repository\EmployeeRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: EmployeeRepository::class)]
class Employee
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private ?string $employeeCode = null;

    /**
     * @var Collection<int, MonthlyActivity>
     */
    #[ORM\OneToMany(targetEntity: MonthlyActivity::class, mappedBy: 'employee', orphanRemoval: true)]
    private Collection $monthlyActivities;

    public function __construct()
    {
        $this->monthlyActivities = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getEmployeeCode(): ?string
    {
        return $this->employeeCode;
    }

    public function setEmployeeCode(string $employeeCode): static
    {
        $this->employeeCode = $employeeCode;

        return $this;
    }

    /**
     * @return Collection<int, MonthlyActivity>
     */
    public function getMonthlyActivities(): Collection
    {
        return $this->monthlyActivities;
    }

    public function addMonthlyActivity(MonthlyActivity $monthlyActivity): static
    {
        if (!$this->monthlyActivities->contains($monthlyActivity)) {
            $this->monthlyActivities->add($monthlyActivity);
            $monthlyActivity->setEmployee($this);
        }

        return $this;
    }

    public function removeMonthlyActivity(MonthlyActivity $monthlyActivity): static
    {
        if ($this->monthlyActivities->removeElement($monthlyActivity)) {
            // set the owning side to null (unless already changed)
            if ($monthlyActivity->getEmployee() === $this) {
                $monthlyActivity->setEmployee(null);
            }
        }

        return $this;
    }
}
