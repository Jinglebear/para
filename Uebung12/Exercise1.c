#include <stdio.h>
#include <mpi.h>
int main(int argc, char *argv[])
{
    int nProcesses, myRank, stag = 1, numIterations = 100, work =1,iteration =0;
    MPI_Comm comm;
    // use world comm
    comm = MPI_COMM_WORLD; 
    // init
    MPI_Init(&argc, &argv);  
    // init comm size
    MPI_Comm_size(comm, &nProcesses);
    // init comm rank
    MPI_Comm_rank(comm, &myRank);
    // programm only operates if there are at least 2 processes
    if (nProcesses >= 2){
        if (myRank == 1 || myRank == 0){
            if (myRank == 0){
                // note Message start time
                double tTotalElapsed = 0;
                double tTotalStart = MPI_Wtime();
                MPI_Status status;
                int message[100], size = 100, dest = 1, source = 1, count=0;
                double messageTime[100];
                // note Total time in Seconds
                // start iteration of ping pong
                while (work){
                    
                    double tMessageStart = MPI_Wtime();
                    // work...
                    MPI_Ssend(&message, size, MPI_INT, dest, stag, comm);
                    //printf("%d : Successfully send!\n", myRank);
                    MPI_Recv(&message, size, MPI_INT, source, stag, comm, &status);
                    //printf("%d : Successfully recieved!\n", myRank);
                    // note end time
                    double tMessageElapsed = MPI_Wtime() - tMessageStart;
                    messageTime[count] = tMessageElapsed;
                    count++;
                    // count up iterations
                    /*
                    One iteration is defined by sending a message, waiting for the accept and waiting for recieving the 
                    returning message 
                    send -- wait(accept) -- recieve -- wait (recieve) 
                     */
                    iteration += 1;
                    if (iteration == numIterations){
                        work = 0;
                        // get the Total time elapsed in Seconds after all Iterations
                    }
                };
                tTotalElapsed = MPI_Wtime() - tTotalStart;
                printf("Total Time elapsed : %lf\n\n\n",tTotalElapsed);
                for(int i=0;i<100;i++){
                    printf("  %lf\n",messageTime[i]);
                }
            }
            else if (myRank == 1){
                MPI_Status status;
                int message[100], size = 100, source = 0, dest = 0;
                while(work) {
                    MPI_Recv(&message, size, MPI_INT, source, stag, comm, &status);
                    //printf("%d : Successfully recieved!\n", myRank);
                    MPI_Ssend(&message, size, MPI_INT, dest, stag, comm);
                    //printf("%d : Successfully send!\n", myRank);
                    iteration += 1;
                    if (iteration == numIterations){
                        work = 0;
                    }
                }
            }
            
        }
    }
    else{
        printf("2 or more Processes required!\n");
    }

    
    MPI_Finalize();
    return 0;
}
