#include "mainwindow_clt.h"
#include "ui_mainwindow_clt.h"

MainWindow_clt::MainWindow_clt(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow_clt)
{
    ui->setupUi(this);
}

MainWindow_clt::~MainWindow_clt()
{
    delete ui;
}

