#include <stdio.h>
#include <mpi.h>
int main(int argc, char *argv[])
{
    int nProcesses, myRank, stag = 1;
    MPI_Status status;
    MPI_Comm comm;

    comm = MPI_COMM_WORLD;
    MPI_Init(&argc,&argv);
    MPI_Comm_size(comm,&nProcesses);
    MPI_Comm_rank(comm,&myRank);


    if(nProcesses >= 2){
        if(myRank == 0){
            //root process
            int message[10], size = 10, dest = 1;
            MPI_Ssend(&message,size,MPI_INT,dest,stag,comm);
            printf("%d : Successfully send!\n",myRank);
            

        }
        else if(myRank == 1){
            int message[10], size = 10, source = 0;
            MPI_Recv(&message,size,MPI_INT,source,stag,comm,&status);
            printf("%d : Successfully recieved!\n",myRank);
        }
    }else{
        printf("2 or more Processes required!\n");
    }



    MPI_Finalize();
    return 0;
}
