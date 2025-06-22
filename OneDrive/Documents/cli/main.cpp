#include "mainwindow_clt.h"

#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    MainWindow_clt w;
    w.show();
    return a.exec();
}
