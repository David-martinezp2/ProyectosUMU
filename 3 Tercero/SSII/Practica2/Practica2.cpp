#include <iostream>
#include <stdlib.h>
#include <fstream>
#include <string>
#include <regex>
#include <vector>
#include <cmath>

using namespace std;


    struct Regla {
        vector<string> condiciones;     // Estructura que representa las reglas BC
        string cons;
        float FC = 0.0;
        };

    struct Hecho {
        string hecho;                   // Estructura que representa las  reglas BH
        float FC = 0.0;
        };

    vector<Regla> BC;   // Vector global que contiene las reglas
    vector<Hecho> BH;  // Vector global que contiene los hechos
    ofstream fout;

    vector<string> elegirCondicion(string linea) {
        regex rgx("([^ ]+)(?: ([^\n]*))?");                 // ER
        smatch matches;
        string aux = linea;                                 // Lo que queda sin analizar por la ER
        vector<string> v;                                   // Vector que devuelve los termino/operando

        while (!aux.empty()) {                              // Se coge el PRIMER termino/operando que hay en aux
            if (regex_search(aux, matches, rgx)) {
                v.push_back(matches[1].str());              // A�ade el termino/operando al vector v
            } else {

                cerr << "Fallo al analizar condiciones de regla" << endl;  // Si la regla no esta bien entonces dara fallo
            }
            aux = "";
            if (!matches[2].str().empty())                  // Revisa si hay mas elementos en la condicion
                aux = matches[2].str();                     // Separam el resto de la condicion para repetir el bucle
        }
        return v;
        }

    bool anadirRegla(string linea) {
        regex rgx("Si ([^ ]+(?: [yo] [^ ]+)*) Entonces (.+), FC=(-?[0-1](?:.[0-9]+)?)"); // Utilizamos la ER para capturar
                                                                                        // los elementos de la regla a partir de cada l�nea del archivo de la BC
        smatch matches;                                                                     // Utilizado para iterar por cada uno de los grupos capturados
        Regla nuevaRegla;                                                                   // Nueva regla que ser� a�adida a la BC
                                                                            // Capturamos los elementos de la regla en funcion de la ER
        if (regex_search(linea, matches, rgx)) {
            nuevaRegla.condiciones = elegirCondicion(matches[1].str());   // Recuperamos la condicion de la regla
            nuevaRegla.cons = matches[2].str();                           // Recuperamos el consecuente de la regla
            nuevaRegla.FC = stof(matches[3].str());                          // Recuperamos el factor de certeza de la regla
        } else {
                                                                        // Si la sintaxis de la regla esta mal entonces fallo
            cerr << "Fallo al analizar una de las reglas de BC\n";
            return false;
        }
        BC.push_back(nuevaRegla);                                               // Añadimos la regla a la BC
        return true;
        }

    bool addHecho(string linea) {
        regex rgx("([^ ]+), FC=(-?[0-1](?:.[0-9]+)?)");  // Utilizamos la ER
        smatch matches;                                     // Utilizado para iterar por cada uno de los grupos capturados
        Hecho nuevoHecho;
                                                            // Capturamos los elementos del hecho en funcion de la ER
        if (regex_search(linea, matches, rgx)) {
            nuevoHecho.hecho = matches[1].str();
            nuevoHecho.FC = stof(matches[2].str());
        } else {
                                                        // Si la sintaxis del hecho esta mal entonces fallo
            cerr << "Fallo al analizar uno de los hechos de BH\n";
            return false;
            }
        BH.push_back(nuevoHecho);                          // Añadimos el hecho a la BH
        return true;
        }

    bool contenida(string objetivo) {
                                                            // Usada para buscar si un hecho esta presente en la BH
        for (unsigned i = 0; i < BH.size(); i++)
            if (BH.at(i).hecho == objetivo)                 // El hecho esta contenido en la BH entonces devolver true
                return true;
        return false;                                       // No se ha encontrado el hecho en la BH entonces devoler false
        }

    vector<int> equiparar(string meta) {
        vector<int> CConflicto;                             // Vector que devolvera la funcion
        for (unsigned i = 0; i < BC.size(); i++) {
            if (BC.at(i).cons == meta)                      // Si "meta" es igual al consecuente de esta regla registrada en la BC...
                CConflicto.push_back(i);                        // Incluir en el Conjunto Conflicto
                }
        return CConflicto;                                 // CConflicto indicara las reglas de la BC que incluyen meta como consecuente
        }

    vector<string> extraerAntecedentes(int R) {
        return BC.at(R).condiciones;                       // Devolver la condicion de la regla R
        }

    float getHechoFC(string objetivo) {
        for (unsigned i = 0; i < BH.size(); i++) {
            if (BH.at(i).hecho == objetivo)                // Si se encuentra el hecho "objetivo" en la BH...
                return BH.at(i).FC;                             // Devolver el FC del hecho
                }
        return 0.0;                                        // Si el hecho no esta presente en la BH devolver FC=0
        }

    float getReglaFC(int R) {
        return BC.at(R).FC;                                // Devolver el FC de la regla R
        }

    float verificar(string meta) {
        int R = -1;
        fout << "\nNuevo objetivo a verificar: " << meta << endl;
        if (contenida(meta)) {
            float f = getHechoFC(meta);
            fout << "Hecho inicial " << meta << " ya presente en la BH con FC=" << f << endl;
            return f;
            }
        vector<int> ConjuntoConflicto = equiparar(meta);
        fout << "Reglas seleccionadas para C conflicto:" << endl;
        for (unsigned i = 0; i < ConjuntoConflicto.size(); i++) {
            fout << "R" << ConjuntoConflicto.at(i)+1 << " ";
            }
        float hechoFC = 0.0;
        while (!ConjuntoConflicto.empty()) {
            R = ConjuntoConflicto.back();
            ConjuntoConflicto.pop_back();
            fout << "\nAnalisis de regla R" << R+1 << " - Condicion de la regla: ";
            vector<string> nuevasMetas = extraerAntecedentes(R);
            for (unsigned i = 0; i < nuevasMetas.size(); i++) {
                fout << nuevasMetas.at(i) << " ";
                }
            fout << endl;
            float ReglaFC = getReglaFC(R);
            float antecedenteFC;
            string operador = "";
            while (!nuevasMetas.empty()) {
                string nuevasMe = nuevasMetas.back();
                nuevasMetas.pop_back();
                if (nuevasMe == "y" || nuevasMe == "o")
                    operador = nuevasMe;
                else {
                    float FC = verificar(nuevasMe);
                                                            // Calculamos el FC de la condicion progresivamente con cada iteracion
                    if (operador == "y") {
                        antecedenteFC = fmin(antecedenteFC, FC);
                        } else if (operador == "o") {
                            antecedenteFC = fmax(antecedenteFC, FC);
                            } else {
                                antecedenteFC = FC;
                                }
                    }
                }
            fout << "Caso 1, FC=" << antecedenteFC << endl;
                                                                    // Para este punto ya he calculado el FC del antecedente y disponemos de los FC regla y consecuente
                                                                    // Aplicamos el caso 3
            float aux = ReglaFC * fmax(0.0, antecedenteFC);
            fout<< "antecedenteFC"<<antecedenteFC;
                                                                    //  Aplicamos el caso 2
                                                                    // Puedo ir aplicando el caso 2 recursivamente utilizando el resultado de la aplicacion del caso 2 en la iteracion anterior
            if (hechoFC >= 0 && aux >= 0){
                hechoFC = hechoFC + aux * (1 - hechoFC);
                }
            else if (hechoFC <= 0 && aux <= 0){
                    hechoFC = hechoFC + aux * (1 + hechoFC);
                    }
            else{
            hechoFC = (hechoFC + aux) / (1 - fmin(abs(hechoFC), abs(aux)));
            }
            fout<<hechoFC<<endl;
            if (ConjuntoConflicto.empty()) {
                fout << "Caso 3,  FC= " << aux << endl;
                fout << "Caso 2,  FC= " << aux << endl;
                Hecho nuevoHecho;
                nuevoHecho.hecho = meta;
                nuevoHecho.FC = hechoFC;
                BH.push_back(nuevoHecho);
                fout << "Hecho " << meta << " verificado con FC=" << hechoFC << endl;
                return hechoFC;
                }
        }
    return 0.0;
    }

    void haciaAtras(string meta) {
        fout << "Objetivo: " << meta << endl << endl;
        float f = verificar(meta);
        fout <<"\nHECHO OBJETIVO "<< meta <<" VERIFICADO CON FC=" << f << endl;
    }

    bool iniBC(string archivo) {
        string linea;                                   // Buffer a donde leeremos cada linea del archivo
        string aux;
        int numRegla;                                    // Numero de reglas que se deberan leer del archivo
        ifstream bc_file;                               // Stream usado para leer el archivo de la BC
        bc_file.open(archivo, ios::in);    // Abrimos el archivo con el contenido de la BC
        if (bc_file.is_open()){                       // archivo abierto correctamente
            getline(bc_file, linea);
            aux = linea.substr(0, linea.length());  // Obtenemos el n�mero de reglas de la 1� linea del archivo
            numRegla = stoi(aux);                        // Parseamos el string a float
            } else {
                                                        // No se ha podido abrir el archivo
                    cerr << "No es posible abrir BC";
                return false;
                    }
        for (int i = 0; i < numRegla; i++) {                   // Por cada una de las reglas
            getline(bc_file, linea);                   // Leemos una nueva linea del archivo de la BC
            if (!anadirRegla(linea))                            // A partir de la linea añadimos la nueva regla a la BH
                return false;
            }
        bc_file.close();                                     // Cerramos el archivo de al BH despues de leer las reglas
        return true;
    }

    string iniBH(string archivo) {
        string linea;                       // Buffer a donde leeremos cada linea del archivo
        string aux;
        int nhecho;                        // N�mero de hechos que se indicar�n en el archivo
        string objetivo = "";                    // Objetivo que se nos indica en el archivo de hechos
        ifstream bh_file;                   // Stream usado para leer el archivo de la BH
                                            // Abrir archivo de la BH
        bh_file.open(archivo, ios::in);
        if (bh_file.is_open()) {
                                            // Buscar como primer par�metro el numero de hechos
            getline(bh_file, linea);
            aux = linea.substr(0, linea.length());
            nhecho = stoi(aux);
            } else {
                                            // No se ha podido abrir el archivo
                cerr << "No es posible abrir BH";
                return "";
                }
                                            // Buscamos los hechos en el archivo
        for (int i = 0; i < nhecho; i++) {
            getline(bh_file, linea);
            if (!addHecho(linea)) {
                bh_file.close();
                return "";
            }
        }
                                            // Leemos el objetivo
        getline(bh_file, linea);
        if (linea == "Objetivo") {
            getline(bh_file, linea);
            objetivo = linea.substr(0, linea.length());
        } else {
                                                // Si el archivo tiene un formato incorrecto devolver error
            cerr << "No se ha logrado reconocer y registrar BH error de formato"
            << endl;
                                                // Cerrar el archivo de la BH
            bh_file.close();
            return "";
            }
                                                // Cerrar el archivo de la BH
        bh_file.close();
        return objetivo;
    }

    int main(int argc, char *argv[]) {
        if(argc!=3) {
            cout << "Error de argumentos " << argc-1 << endl;
            cout << "OK: "<< argv[0] <<" <archivo de BC> <archivo de BH>" << endl;
            return -1;
            }
                                                // Preparamos la BC
        if (!iniBC(argv[1]))
                                            // Error en la inicializacion de la BC entonces -1
            return -1;
                                                // Preparamos la BH
        string objetivo = iniBH(argv[2]);
                                                    // Error en la inicializacion de la BH entonces -1
        if (objetivo.empty())
            return -1;
        string f1=argv[1];
        string f2=argv[2];
        fout.open (f1.substr(0,f1.length()-4)+f2, ios::out | ios::binary | ios::trunc);
        fout << "Nombre BC: " << argv[1] << endl;
        fout << "Nombre BH: " << argv[2] << endl;
                                                // Iniciar el motor de inferencia
        haciaAtras(objetivo);
        return 0;
    }

