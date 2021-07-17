#include <stdio.h>
#include <mpi.h>
int main(int argc, char *argv[])
{
    int nProcesses, myRank, stag = 1;
    MPI_Comm comm;

    comm = MPI_COMM_WORLD;
    MPI_Init(&argc,&argv);
    MPI_Comm_size(comm,&nProcesses);
    MPI_Comm_rank(comm,&myRank);


    if(nProcesses >= 2){
        if(myRank == 1 || myRank == 0) {
            double stime = MPI_Wtime();
            int work = 1;
            while(work) {
                if(myRank == 0){
                    //root process
                    MPI_Status status;
                    int message[100], size = 100, dest = 1, source =1;
                    for(int i=0;i<size;++i){
                        message[i] = (i * 512)+1;
                    }
                    MPI_Ssend(&message,size,MPI_INT,dest,stag,comm);
                    printf("%d : Successfully send!\n",myRank);
                    MPI_Recv(&message,size,MPI_INT,source,stag,comm,&status);
                    printf("%d : Successfully recieved!\n",myRank);
        
                }
                else if(myRank == 1){
                    MPI_Status status;
                    int message[100], size = 100, source = 0, dest =0;
                    MPI_Recv(&message,size,MPI_INT,source,stag,comm,&status);
                    /* for(int j=0;j<size;++j){
                        printf("%d,",message[j]);
                    } */
                    printf("%d : Successfully recieved!\n",myRank);
                    for(int i=0;i<size;++i){
                        message[i] = (i * 512)+1;
                    }
                    MPI_Ssend(&message,size,MPI_INT,dest,stag,comm);
                    printf("%d : Successfully send!\n",myRank);
                }
                if(MPI_Wtime() > stime + 0){
                    work =0;
                    MPI_Finalize();
                    return 0;
                }
            }
        }
    }else{
        printf("2 or more Processes required!\n");
    }



    MPI_Finalize();
    return 0;
}
