#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "client.h"
#include "connection.h"
#include "arduino_simulation.h"
#include <QtCharts>
#include <QMessageBox>
#include <QValidator>
#include <QIntValidator>
#include <QSqlQuery>
#include <QSqlError>
#include <QDebug>
#include <QFileDialog>
#include <QPrinter>
#include <QPainter>
#include <QtCharts/QChartView>
#include <QtCharts/QChart>
#include <QVBoxLayout>
#include <QtCharts/QPieSeries>
#include <QSortFilterProxyModel>
#include <QPlainTextEdit>
#include <QStandardItem>
#include <QtCharts/QChartView>
#include <QtCharts/QPieSeries>
#include <QtCharts/QChart>
#include <QtWidgets/QVBoxLayout>
#include <QSqlTableModel>
MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    int ret=A.connect_arduino();
    switch(ret)
    {
    case(0):qDebug()<<"arduino is availble and connected to :"<<A.getarduino_port_name();
    break;
    case(1):qDebug()<<"arduino is vailable but not connected to:"<<A.getarduino_port_name();
    break;
    case(-1):qDebug()<<"arduino is not available";

    }
    QObject::connect(A.getserial(),SIGNAL(readyRead()),this,SLOT(update_label()));
    connection = new Connection();

    if (!connection->createconnect()) {
        QMessageBox::critical(this, "Database Error", "Failed to connect to the database");
    }
    ui->tab_client->setModel(C.afficher());

    ui->CIN_LE->setValidator(new QIntValidator(10000000, 99999999, this));
    QDoubleValidator* reponseValidator = new QDoubleValidator(0.0, 5,1, this);


    ui->reponse1->setValidator(reponseValidator);
    ui->reponse2->setValidator(reponseValidator);
    ui->reponse3->setValidator(reponseValidator);
    ui->reponse4->setValidator(reponseValidator);

}

MainWindow::~MainWindow()
{
    delete ui;
}


void MainWindow::on_pb_ajouter_clicked() {
    int CIN = ui->CIN_LE->text().toInt();
    int numTl = ui->numTl__LE->text().toInt();
    double prix = ui->prix->text().toFloat();
    QString nom = ui->nom_LE->text();
    QString prenom = ui->prenom_LE->text();
    QString adresse = ui->adresse_LE->text();
    QString type_achat = ui->type_achat->toPlainText();


    Client C(CIN, numTl, prix,nom, prenom, adresse,type_achat);


    bool test = C.ajouter();


        if (test) {
             ui->tab_client->setModel(C.afficher());
             QMessageBox::information(nullptr, QObject::tr("Succès"),
                 QObject::tr("Ajout effectué\n"
                             "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);
        }




        else {

        QMessageBox::critical(nullptr, QObject::tr("Erreur"),
            QObject::tr("Ajout client non effectué\n"
                        "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);
    }
}




void MainWindow::on_pb_supp_clicked()
{
    Client C; C.setCIN(ui->supp->text().toInt());
    bool test=C.supprimer(C.getCIN());
    
    if (test)
    {
        ui->tab_client->setModel(C.afficher());
        QMessageBox::information(nullptr, QObject::tr("ok"),
            QObject::tr("suppression effectué\n"
                        "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);

    }
    else
    {
        QMessageBox::critical(nullptr, QObject::tr("notOk"),
            QObject::tr("suppression non effectué\n"
                        "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);
    }
}

void MainWindow::on_modifier_clicked()
{
    int CIN = ui->CIN_LE->text().toInt();
    int numTl = ui->numTl__LE->text().toInt();
    double prix = ui->prix->text().toFloat();
    QString nom = ui->nom_LE->text();
    QString prenom = ui->prenom_LE->text();
    QString adresse = ui->adresse_LE->text();
    QString type_achat = ui->type_achat->toPlainText();
    Client C(CIN, numTl,prix, nom, prenom, adresse,type_achat);
    bool test = C.modifier(CIN);
    if (test)
    {
        ui->tab_client->setModel(C.afficher());
        QMessageBox::information(nullptr, QObject::tr("Succès"),
            QObject::tr("modifier effectué\n"
                        "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);
    }
    else
    {
        QMessageBox::critical(nullptr, QObject::tr("Erreur"),
            QObject::tr("modifier non effectué\n"
                        "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);
    }

}


void MainWindow::on_trier_clicked()
{
    QString sortParam = ui->trier_selon->currentText(); // Get the selected sort parameter

       QSqlQueryModel *sortedModel = new QSqlQueryModel();

       if (sortParam == "CIN") {
           sortedModel->setQuery("SELECT * FROM CLIENT ORDER BY CIN ASC");
       } else if (sortParam == "nom") {
           sortedModel->setQuery("SELECT * FROM CLIENT ORDER BY nom ASC");
       } else if (sortParam == "prenom") {
           sortedModel->setQuery("SELECT * FROM CLIENT ORDER BY prenom ASC");
       }

       ui->tab_client->setModel(sortedModel);
}

void MainWindow::on_PDF_clicked()
{
    QString fileName = QFileDialog::getSaveFileName(this, "Save PDF", "", "PDF Files (*.pdf)");

        if (fileName.isEmpty())
            return;

        QPrinter printer(QPrinter::PrinterResolution);
        printer.setOutputFormat(QPrinter::PdfFormat);
        printer.setOutputFileName(fileName);

        QPainter painter(&printer);
        painter.setRenderHint(QPainter::Antialiasing, true);

        QAbstractItemModel *model = ui->tab_client->model();
        int rowCount = model->rowCount();
        int columnCount = model->columnCount();

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                QModelIndex index = model->index(row, column);
                QString text = model->data(index, Qt::DisplayRole).toString();
                painter.drawText(50 + column * 120, 50 + row * 20, text);
            }
        }

        painter.end();

        QMessageBox::information(this, "PDF Export", "PDF exported to " + fileName);
}


void MainWindow::on_STAT_clicked()

{

    // Créer un objet QChartView pour afficher le graphique

        QChartView *chartView = new QChartView(this);

        QChart *chart = new QChart();



        // Obtenir les statistiques du nombre d'employés par rôle

        QMap<QString, int> roleCountMap;



        QSqlQuery query;

        query.prepare("SELECT PRIX, COUNT(*) FROM CLIENT GROUP BY PRIX");

        if (query.exec()) {

            while (query.next()) {

                QString role = query.value(0).toString();

                int count = query.value(1).toInt();

                roleCountMap[role] = count;

            }

        }



        // Créer une série de données pour le graphique circulaire

        QPieSeries *series = new QPieSeries();

        int totalCount = 0;

        for (const QString &role : roleCountMap.keys()) {

            totalCount += roleCountMap[role];

        }



        for (const QString &role : roleCountMap.keys()) {

            double percentage = static_cast<double>(roleCountMap[role]) / totalCount * 100;

            QPieSlice *slice = series->append(role, roleCountMap[role]);

            slice->setLabel(QString("%1\n%2%").arg(role).arg(percentage, 0, 'f', 1));

        }



        // Ajouter la série au graphique

        chart->addSeries(series);



        // Créer le graphique circulaire

        chartView->setChart(chart);



        // Afficher le graphique dans une nouvelle fenêtre

        QMainWindow *chartWindow = new QMainWindow(this);

        chartWindow->setCentralWidget(chartView);

        chartWindow->resize(800, 600);

        chartWindow->show();

}



/*void MainWindow::on_chercher_clicked()
{
    int cinRecherche = ui->CIN_chercher->text().toInt();

    Client client;
    QSqlQueryModel* resultat = client.chercherParCIN(cinRecherche);

    if (resultat != nullptr)
    {
        ui->tab_client->setModel(resultat);
        QMessageBox::information(nullptr, QObject::tr("Succès"),
            QObject::tr("Recherche effectuée\n"
                        "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);
    }
    else
    {
        QMessageBox::critical(nullptr, QObject::tr("Erreur"),
            QObject::tr("Recherche non effectuée\n"
                        "Cliquez sur Annuler pour fermer."), QMessageBox::Cancel);
    }
}*/
void MainWindow::on_chercher_clicked() {
    QString searchParam = ui->chercher_selon->currentText(); // Get the selected search parameter
    QString searchText = ui->lineEditSearch->text(); // Get the text to search

    QSqlQueryModel *searchResultModel = new QSqlQueryModel();

    if (searchParam == "nom") {
        searchResultModel->setQuery(QString("SELECT * FROM CLIENT WHERE nom = '%1'").arg(searchText));
    }
    else
        if (searchParam == "adresse") {
            searchResultModel->setQuery(QString("SELECT * FROM CLIENT WHERE adresse= '%1'").arg(searchText));}
        else if (searchParam == "CIN") {
        bool isNumeric;
        int searchCIN = searchText.toInt(&isNumeric);

        if (isNumeric) {
            searchResultModel->setQuery(QString("SELECT * FROM CLIENT WHERE CIN = %1").arg(searchCIN));
        } else {
            // Handle non-numeric input for ID (optional)
            // Show an error message or take appropriate action
        }
    }

    ui->tab_client->setModel(searchResultModel);
}





void MainWindow::on_ajouter_achat_clicked()
{
    // Retrieve information from the interface
    int CIN = ui->CIN->text().toInt();
    QString typeAchat = ui->type_achat_ajouter->toPlainText();
    double prixAchat = ui->prix_ajouter->text().toDouble();

    // Check if the client with the CIN already exists
    QSqlQuery checkQuery;
    checkQuery.prepare("SELECT CIN, type_achat, prix FROM CLIENT WHERE CIN = :CIN");
    checkQuery.bindValue(":CIN", CIN);

    if (!checkQuery.exec()) {
        handleDatabaseError(checkQuery.lastError(), "Vérification du client échouée");
        return;
    }

    if (checkQuery.next()) {
        // The client with the CIN exists, retrieve existing values
        QString existingTypeAchat = checkQuery.value("type_achat").toString();
        double existingPrix = checkQuery.value("prix").toDouble();

        // Update type of purchase and total price
        QString updatedTypeAchat = concatenateValues(existingTypeAchat, typeAchat);
        double updatedPrix = existingPrix + prixAchat;

        // Perform the update
        if (!updateClientPurchase(CIN, updatedTypeAchat, updatedPrix)) {
            return;
        }

        // Update the UI display
        ui->tab_client->setModel(C.afficher());

        // Notify the user about the successful update
        QMessageBox::information(nullptr, tr("Succès"),
            tr("Mise à jour d'achat effectuée avec succès."), QMessageBox::Ok);
    } else {
        // The client with the CIN does not exist
        QMessageBox::warning(nullptr, tr("Avertissement"),
            tr("Le client avec le CIN spécifié n'existe pas."), QMessageBox::Ok);
    }
}

void MainWindow::handleDatabaseError(const QSqlError& error, const QString& message)
{
    qDebug() << "Database error:" << error.text();
    QMessageBox::critical(nullptr, tr("Erreur"),
        tr("%1. Erreur de base de données:\n%2").arg(message).arg(error.text()),
        QMessageBox::Cancel);
}

QString MainWindow::concatenateValues(const QString& existingValue, const QString& newValue)
{
    // Concatenate the new value with the existing one (adjust this based on your data model)
    return existingValue.isEmpty() ? newValue : existingValue + ", " + newValue;
}

bool MainWindow::updateClientPurchase(int CIN, const QString& typeAchat, double prixAchat)
{
    // Perform the update query
    QSqlQuery updateQuery;
    updateQuery.prepare("UPDATE CLIENT SET type_achat = :type_achat, prix = :prix WHERE CIN = :CIN");
    updateQuery.bindValue(":CIN", CIN);
    updateQuery.bindValue(":type_achat", typeAchat);
    updateQuery.bindValue(":prix", prixAchat);

    if (!updateQuery.exec()) {
        handleDatabaseError(updateQuery.lastError(), "Mise à jour d'achat non effectuée");
        return false;
    }

    return true;
}





/*
void MainWindow::on_ajouter_achat_clicked()
{
    // Retrieve information from the interface
    int CIN = ui->CIN->text().toInt();
    QString typeAchat = ui->type_achat_ajouter->toPlainText();

    double prixAchat = ui->prix_ajouter->text().toDouble();

    // Check if the client with the CIN already exists
    QSqlQuery checkQuery;
    checkQuery.prepare("SELECT CIN, type_achat, prix FROM CLIENT WHERE CIN = :CIN");
    checkQuery.bindValue(":CIN", CIN);

    if (!checkQuery.exec()) {
        handleDatabaseError(checkQuery.lastError(), "Vérification du client échouée");
        return;
    }


    if (checkQuery.next()) {
         // The client with the CIN exists, retrieve existing values
         QString existingTypeAchat = checkQuery.value("type_achat").toString();
         double existingPrix = checkQuery.value("prix").toDouble();

         // Update type of purchase and total price
         QString updatedTypeAchat = concatenateValues(existingTypeAchat, typeAchat);
         double updatedPrix = existingPrix + prixAchat;

         // Perform the update
         if (!updateClientPurchase(CIN, updatedTypeAchat, updatedPrix)) {
             return;
         }

         // Update the model directly using the returned model from afficher
         ui->tab_client->setModel(C.afficher());

         // Notify the user about the successful update
         QMessageBox::information(nullptr, tr("Succès"),
             tr("Mise à jour d'achat effectuée avec succès."), QMessageBox::Ok);
     } else {
         // The client with the CIN does not exist
         QMessageBox::warning(nullptr, tr("Avertissement"),
             tr("Le client avec le CIN spécifié n'existe pas."), QMessageBox::Ok);
     }
 }

QString MainWindow::concatenateValues(const QString& existingValue, const QString& newValue)
{
        return existingValue.isEmpty() ? newValue : existingValue + "<br>" + newValue;

}

bool MainWindow::updateClientPurchase(int CIN, const QString& typeAchat, double prixAchat)
{
    // Perform the update query
    QSqlQuery updateQuery;
    updateQuery.prepare("UPDATE CLIENT SET type_achat = :type_achat, prix = :prix WHERE CIN = :CIN");
    updateQuery.bindValue(":CIN", CIN);
    updateQuery.bindValue(":type_achat", typeAchat);
    updateQuery.bindValue(":prix", prixAchat);

    if (!updateQuery.exec()) {
        handleDatabaseError(updateQuery.lastError(), "Mise à jour d'achat non effectuée");
        return false;
    }

    return true;
}
*/


void MainWindow::on_fidelite_clicked()
{
    updateLoyaltyInfoTable();
}



QString MainWindow::loyaltyAnalysis(const QString& typeAchat, double prix)
{
    // Implement your loyalty analysis logic here
    // This is a placeholder, replace it with your actual logic
    int numArticles = typeAchat.split(',').size(); // Assuming typeAchat is a comma-separated list of articles
    if (prix >= 3000 && numArticles >= 5) {
        return "Platinum";
    } else if (prix >= 2000 && numArticles >= 3) {
        return "Gold";
    } else if (prix >= 1000 && numArticles >= 1) {
        return "Silver";
    } else {
        return "Bronze";
    }
}

void MainWindow::updateLoyaltyInfoTable()
{
    QSqlQuery query;
    if (query.exec("SELECT CIN, nom, prenom, type_achat, prix FROM client")) {
        // Assuming you have a table named "loyalty_info" with columns CIN, nom, prenom, and loyalty_class
        QSqlQuery deleteQuery;
        if (!deleteQuery.exec("DELETE FROM loyalty_info")) {
            QMessageBox::critical(this, "Database Error", "Failed to clear loyalty_info table");
            return;
        }

        while (query.next()) {
            int CIN = query.value(0).toInt();
            QString nom = query.value(1).toString();
            QString prenom = query.value(2).toString();
            QString typeAchat = query.value(3).toString();
            double prix = query.value(4).toDouble();

            QString loyaltyClass = loyaltyAnalysis(typeAchat, prix);

            // Insert the loyalty information into the loyalty_info table
            QSqlQuery insertQuery;
            if (!insertQuery.exec(QString("INSERT INTO loyalty_info (CIN, nom, prenom, loyalty_class) "
                                          "VALUES (%1, '%2', '%3', '%4')")
                                          .arg(CIN)
                                          .arg(nom)
                                          .arg(prenom)
                                          .arg(loyaltyClass))) {
                QMessageBox::critical(this, "Database Error", "Failed to insert loyalty information");
            }
        }

        // Display the loyalty information in the loyaltyTableView
        QSqlTableModel *loyaltyModel = new QSqlTableModel(this, connection->getDatabase());  // Use connection->getDatabase()
        loyaltyModel->setTable("loyalty_info");
        loyaltyModel->select();

        ui->loyaltyTableView->setModel(loyaltyModel);
    } else {
        QMessageBox::critical(this, "Database Error", "Failed to execute query");
    }
}
void MainWindow::on_calculer_clicked()
{
    int reponse1 = ui->reponse1->text().toInt();
    int reponse2 = ui->reponse2->text().toInt();
    int reponse3 = ui->reponse3->text().toInt();
    int reponse4 = ui->reponse4->text().toInt();


    double moyenne = (reponse1 + reponse2 + reponse3 + reponse4) / 4.0;
    results.append(qMakePair(moyenne, QList<int>{reponse1, reponse2, reponse3, reponse4}));

    ui->moyenne->setText("Moyenne : " + QString::number(moyenne));
    ui->resultTextEdit->appendPlainText("Moyenne: " + QString::number(moyenne) +
                                          ", Réponses: " + QString::number(reponse1) +
                                          ", " + QString::number(reponse2) +
                                          ", " + QString::number(reponse3) +
                                          ", " + QString::number(reponse4));
}


void MainWindow::on_afficher_2_clicked()
{
   ui->tab_client->setModel(C.afficher());
}
void MainWindow::update_label()
{
    // Read RFID card UID from Arduino
    QString cardUID = A.read_from_arduino();

    // Update loyalty points based on the card UID
    updatePoints(cardUID);

    // Update label text based on the loyalty points or any other relevant information
    int loyaltyPoints = getLoyaltyPoints(cardUID);  // You need to implement this function
    ui->label_11->setText("Loyalty Points: " + QString::number(loyaltyPoints));
}

// Function to update loyalty points based on RFID card UID
void MainWindow::updatePoints(QString cardUID) {
    QSqlQuery query;

    // Check if the card UID exists in the database
    query.prepare("SELECT * FROM Client WHERE CardUID = :cardUID");
    query.bindValue(":cardUID", cardUID);

    if (query.exec() && query.next()) {
        // Card UID found, update the nbre_point column
        int currentPoints = query.value("nbre_point").toInt();
        currentPoints += 10;

        // Update nbre_point in the database
        query.prepare("UPDATE ClientTable SET nbre_point = :points WHERE CardUID = :cardUID");
        query.bindValue(":points", currentPoints);
        query.bindValue(":cardUID", cardUID);

        query.exec();  // Consider handling errors in a real-world scenario
    }
}

// Function to retrieve loyalty points based on RFID card UID
int MainWindow::getLoyaltyPoints(QString cardUID) {
    QSqlQuery query;

    // Check if the card UID exists in the database
    query.prepare("SELECT nbre_point FROM ClientTable WHERE CardUID = :cardUID");
    query.bindValue(":cardUID", cardUID);

    if (query.exec() && query.next()) {
        // Card UID found, return the nbre_point
        return query.value("nbre_point").toInt();
    }

    // Card UID not found, return a default value or handle accordingly
    return 0;
}

