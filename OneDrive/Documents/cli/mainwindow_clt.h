#ifndef MAINWINDOW_CLT_H
#define MAINWINDOW_CLT_H

#include <QMainWindow>

QT_BEGIN_NAMESPACE
namespace Ui { class MainWindow_clt; }
QT_END_NAMESPACE

class MainWindow_clt : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow_clt(QWidget *parent = nullptr);
    ~MainWindow_clt();

private:
    Ui::MainWindow_clt *ui;
};
#endif // MAINWINDOW_CLT_H
