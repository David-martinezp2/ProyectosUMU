/* ------------------------- PROBLEMA DE LAS N REINAS ----------------------- */
#include <ga/GASimpleGA.h> //  Algoritmo Genetico simple
#include <ga/GA1DArrayGenome.h> // Genoma --> array de enteros (dim. 1) alelos
#include <iostream>
#include <fstream>
using namespace std;
#include <stdio.h>
#include <time.h>
#include <wchar.h>
#include <locale.h>

float Objective(GAGenome &); // Funcion objetivo --> al final

GABoolean Termina(GAGeneticAlgorithm &); // Funcion de terminacion --> al final

void mostrarTablero(GAGenome &);

int main(int argc, char **argv)
{
    setlocale(LC_ALL, "");
    clock_t comienzo = clock();

    int nreinas=atoi(argv[1]);
    cout << "Problema de las " << nreinas << " reinas \n\n";

// Declaramos variables para los parametros del GA y las inicializamos

    int popsize = atoi(argv[2]);
    int ngen = atoi(argv[3]);
    float pcross = atof(argv[4]);
    float pmut = atof(argv[5]);
    cout << "Parámetros: (" << popsize << ", " << ngen << ", " << pcross << ", " << pmut << ")" << endl << endl;

// Conjunto enumerado de alelos --> valores posibles de cada gen del genoma

    GAAlleleSet<int> alelos;
    for(int i=0;i<nreinas;i++) alelos.add(i);

// Creamos el genoma y definimos operadores de inicio, cruce y mutación

    GA1DArrayAlleleGenome<int> genome(nreinas,alelos,Objective,NULL);
    genome.crossover(GA1DArrayAlleleGenome<int>::OnePointCrossover);
    genome.mutator(GA1DArrayAlleleGenome<int>::FlipMutator);

// Creamos el algoritmo genetico

    GASimpleGA ga(genome);

// Inicializamos - minimizar funcion objetivo, tamaño poblacion, nº generaciones,
// pr. cruce y pr. mutacion, selección y le indicamos que evolucione.

    ga.minimaxi(-1);
    ga.populationSize(popsize);
    ga.nGenerations(ngen);
    ga.pCrossover(pcross);
    ga.pMutation(pmut);
    GATournamentSelector selector;
    ga.selector(selector);
    ga.terminator(Termina);
    ga.evolve(1);

// Imprimimos el mejor individuo que encuentra el GA y su valor fitness

    cout << "El sistema encuentra la solución " << endl;
    GA1DArrayAlleleGenome<int> & g = (GA1DArrayAlleleGenome<int> &)ga.statistics().bestIndividual();
    mostrarTablero(g);
    cout << "con " << ga.statistics().minEver() << " jaques. " << endl;

    //imprimimos el valor fitness
    int aux=0;
    for(int i=0;i<nreinas; i++){
        aux +=i;
    }
    cout << "Fitness:"<< aux-ga.statistics().minEver()<<endl<<endl;

    cout << "Tiempo transcurrido " << ((double)(clock()-comienzo))/CLOCKS_PER_SEC << " s" << endl;
    cout << " " << endl;
}

// Funcion objetivo.

float Objective(GAGenome& g) {
    GA1DArrayAlleleGenome<int> & genome = (GA1DArrayAlleleGenome<int> &)g;
    float jaques=0;

    for(int i=0; i<genome.length(); i++)
       for(int j=i+1;j<genome.length();j++)
            if ((genome.gene(i)==genome.gene(j))||(abs(j-i)==abs(genome.gene(j)-genome.gene(i)))) jaques++;
    return jaques;
}

// Funcion de terminacion

GABoolean Termina(GAGeneticAlgorithm & ga){
    if ( (ga.statistics().minEver()==0) ||
        (ga.statistics().generation()==ga.nGenerations()) ) return gaTrue;
    else return gaFalse;
}

void mostrarTablero (GAGenome& g)
{
  int i,j,col,n;

  GA1DArrayAlleleGenome<int> & genome = (GA1DArrayAlleleGenome<int> &)g;
  n = genome.length();

  for (i=0; i<n; i++) {
      for (j=0; j<n; j++) {
          if (genome.gene(j)==i) {
                cout << "R ";
                col = j;
          }
          else cout << "- ";
      }
      cout << " " << i+1 << " " << col+1 << endl;
  }
  cout << endl;
}