<?php

namespace App\Entity;

use App\Repository\MonthlyActivityRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: MonthlyActivityRepository::class)]
class MonthlyActivity
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'monthlyActivities')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Employee $employee = null;

    #[ORM\Column(length: 255)]
    private ?string $monthName = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2)]
    private ?string $actualResourceUsage = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2)]
    private ?string $actualInternalRvenue = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getEmployee(): ?Employee
    {
        return $this->employee;
    }

    public function setEmployee(?Employee $employee): static
    {
        $this->employee = $employee;

        return $this;
    }

    public function getMonthName(): ?string
    {
        return $this->monthName;
    }

    public function setMonthName(string $monthName): static
    {
        $this->monthName = $monthName;

        return $this;
    }

    public function getActualResourceUsage(): ?string
    {
        return $this->actualResourceUsage;
    }

    public function setActualResourceUsage(string $actualResourceUsage): static
    {
        $this->actualResourceUsage = $actualResourceUsage;

        return $this;
    }

    public function getActualInternalRvenue(): ?string
    {
        return $this->actualInternalRvenue;
    }

    public function setActualInternalRvenue(string $actualInternalRvenue): static
    {
        $this->actualInternalRvenue = $actualInternalRvenue;

        return $this;
    }
}
