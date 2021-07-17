#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>


//Function for Synchronized Send:
void Test(int type, int size, int iterations, int myRank) {
    int work = 1, stag = 1, iteration = 0;
    MPI_Comm comm = MPI_COMM_WORLD;
    // programm only operates if there are at least 2 processes
	MPI_Barrier(comm);
    if (myRank == 1 || myRank == 0){
        if (myRank == 0){
            // note Message start time
            int bsize = size*sizeof(int) + MPI_BSEND_OVERHEAD;
            char* bufptr = malloc(bsize);
            double tTotalElapsed = 0;
            double tTotalStart = MPI_Wtime();
            MPI_Status status;
            int dest = 1, source = 1, count=0;
            int *message;
            message = (int *)malloc(size * sizeof(int));
            double messageTime[iterations];
            // note Total time in Seconds
            // start iteration of ping pong
            while (work){

                double tMessageStart = MPI_Wtime();
                // work...
                if(type == 0)
                    MPI_Send(message, 1, MPI_INT, dest, stag, comm);
                else if(type == 1)
                    MPI_Ssend(message, 1, MPI_INT, dest, stag, comm);
                else if(type == 2) {
                    MPI_Buffer_attach(bufptr, size + MPI_BSEND_OVERHEAD);
                    //printf("Got so far\n");
                    fflush(stdout);
                    MPI_Bsend(message, 1, MPI_INT, dest, stag, comm);
                    //printf("Got so far as well\n");
                    fflush(stdout);
                    MPI_Buffer_detach(&bufptr, &bsize);
                }
                //printf("%d : Successfully send!\n", myRank);
                MPI_Recv(message, 1, MPI_INT, source, stag, comm, &status);
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
                if (iteration == iterations){
                    work = 0;
                    // get the Total time elapsed in Seconds after all Iterations
                }
            };
            tTotalElapsed = MPI_Wtime() - tTotalStart;
            int byteSize = size * sizeof(int);
            printf("Size (bytes): %d\n",byteSize);
            printf("Num of Iterations : %d\n",iterations);
            printf("Total Time elapsed (in Seconds): %lf\n",tTotalElapsed);
            double avgMessageTime =0;
            for(int i = 0; i < iterations; i++){
                avgMessageTime += messageTime[i];
            }
            avgMessageTime = avgMessageTime / iterations;
            printf("Average Message Time (in Seconds): %lf\n", avgMessageTime);
            printf("\n\n");
        }
        else if (myRank == 1){
            int bsize = size*sizeof(int) + MPI_BSEND_OVERHEAD;
            char* bufptr = malloc(bsize);
            MPI_Status status;
            int source = 0, dest = 0;
            int *message;
            message = (int *)malloc(size * sizeof(int));
            while(work) {
                MPI_Recv(message, 1, MPI_INT, source, stag, comm, &status);
                //printf("%d : Successfully recieved!\n", myRank);
                if(type == 0)
                    MPI_Send(message, 1, MPI_INT, dest, stag, comm);
                else if(type == 1)
                    MPI_Ssend(message, 1, MPI_INT, dest, stag, comm);
                else if(type == 2) {
                    MPI_Buffer_attach(bufptr, size + MPI_BSEND_OVERHEAD);
                    MPI_Bsend(message, 1, MPI_INT, dest, stag, comm);
                    MPI_Buffer_detach(&bufptr, &bsize);
                }
                //printf("%d : Successfully send!\n", myRank);
                iteration += 1;
                if (iteration == iterations){
                    work = 0;
                }
            }
        }

    }
}

void MultiTest(int size, int iterations, int myRank)
{
    MPI_Comm comm = MPI_COMM_WORLD;
    MPI_Status status;
    int n;
    int iteration = 0;
    int work = 1;
    MPI_Comm_size(comm, &n);
    MPI_Barrier(comm);
    double startTime = MPI_Wtime();
    double totalTime = 0;

    int* message = (int*) malloc(size * sizeof(int));
    int stag = 1;

    if(myRank == 0) {
        printf("MultiTest with %d Processes:\n", n);
        fflush(stdout);
        int messageTimes[iterations];
        while(work) {
            int i;
            for(i = 1; i < n; ++i)
                MPI_Send(message, 1, MPI_INT, i, stag, comm);
            for(i = 1; i < n; ++i)
                MPI_Recv(message, 1, MPI_INT, i, stag, comm, &status);
            ++iteration;
            if(iteration == iterations)
                work = 0;
            double elapsedTime = MPI_Wtime() - startTime;
            startTime = elapsedTime;
            totalTime += elapsedTime;
            messageTimes[iteration] = elapsedTime;
        }
        //print out result of Test
        double avgMessageTime = 0;
        int i;
        for(i = 0; i < iterations; ++i)
            avgMessageTime += messageTimes[i];
        avgMessageTime /= iterations;

        printf("Size (bytes): %d\n", size*sizeof(int));
        printf("Num of Iterations : %d\n", iterations);
        printf("Total Time elapsed (in Seconds): %lf\n", totalTime);
        printf("Average Message Time (in Seconds): %lf\n", avgMessageTime);
        printf("\n\n");
    }
    else if(myRank > 0 && myRank < n){
        while(work) {
            MPI_Recv(message, 1, MPI_INT, 0, stag, comm, &status);
            MPI_Send(message, 1, MPI_INT, 0, stag, comm);
            ++iterations;
            if(iterations == iteration)
                work = 0;
        }
    }
}

int main(int argc, char *argv[])
{
    MPI_Init(&argc, &argv);

    //Set up environment:
    int nProcesses, myRank, iterations = 10000;
    MPI_Comm comm;
    // use world comm
    comm = MPI_COMM_WORLD;
    // init comm size
    MPI_Comm_size(comm, &nProcesses);
    // init comm rank
    MPI_Comm_rank(comm, &myRank);

    // programm only operates if there are at least 2 processes
    if(nProcesses >= 2)
    {

        int type = 2;   //0: send, 1: synchronized send, 2: buffered send, 3: send to many
        int sizes[] = {1, 5, 10, 100, 1000, 2000, 5000, 10000, 12000, 50000, 100000, 200000, 300000, 500000, 800000};
        int length = sizeof(sizes)/sizeof(int);
        int i;
        if(type > -1 && type < 3)
            for(i = 0; i < length; ++i)
                Test(type, sizes[i], iterations, myRank);
        else if(type == 3)
            for(i = 0; i < length; ++i)
                MultiTest(sizes[i], iterations, myRank);

    }
    else
    {
        printf("Not enough Processes\n");
    }


    MPI_Finalize();
    return 0;
}
