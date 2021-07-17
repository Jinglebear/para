#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>

//op_type: 0:add, 1: multiply, 2: maximum, 3: minimum
int reduce(void* sendbuf, void* recvbuf, int count, int op_type, int root, MPI_Comm comm)
{
    int n;
    MPI_Comm_size(MPI_COMM_WORLD, &n);
    int myRank;
    MPI_Comm_rank(MPI_COMM_WORLD, &myRank);
    MPI_Status status;

    if(myRank == root) {
        for(int i = 0; i < n; ++i)
        {
            if(i != root) {
                int res[count];
                MPI_Recv(res, count, MPI_INT, i, 1, comm, &status);
                for(int j = 0; j < count; ++j) {
                    //Perform operation;
                    if(op_type == 0)    //Addition
                        ((int*)recvbuf)[j] += res[j];
                    else if(op_type == 1)   //Multiplication
                        if(i == 0)
                            ((int*)recvbuf)[j] = res[j];
                        else
                            ((int*)recvbuf)[j] *= res[j];
                    else if(op_type == 2)   //Maximum
                        if(i == 0)
                            ((int*)recvbuf)[j] = res[j];
                        else
                            if(res[j] > ((int*)recvbuf)[j])
                                ((int*)recvbuf)[j] = res[j];
                    else if(op_type == 3)   //Minimum
                        if(i == 0)
                            ((int*)recvbuf)[j] = res[j];
                        else
                            if(res[j] < ((int*)recvbuf)[j])
                                ((int*)recvbuf)[j] = res[j];
                }
            }
            else {
                for(int j = 0; j < count; ++j)
                    if(op_type == 0)    //Addition
                        ((int*)recvbuf)[j] += ((int*)sendbuf)[j];
                    else if(op_type == 1)
                        if(i == 0)      //Multiplication
                            ((int*)recvbuf)[j] = ((int*)sendbuf)[j];
                        else
                            ((int*)recvbuf)[j] *= ((int*)sendbuf)[j];
                    else if(op_type == 2)
                         if(i == 0) //Maximum
                            ((int*)recvbuf)[j] = ((int*)sendbuf)[j];
                        else
                            if(((int*)sendbuf)[j] > ((int*)recvbuf)[j])
                                ((int*)recvbuf)[j] = ((int*)sendbuf)[j];
                    else if(op_type == 3)   //Minimum
                        if(i == 0)
                            ((int*)recvbuf)[j] = ((int*)sendbuf)[j];
                        else
                            if(((int*)sendbuf)[j] < ((int*)recvbuf)[j])
                                ((int*)recvbuf)[j] = ((int*)sendbuf)[j];
            }
        }
    }

    if(myRank != root) {
        MPI_Send(sendbuf, count, MPI_INT, root, 1, comm);
    }

    return 0;
}


int main(int argc, char** argv) {

    MPI_Init(&argc, &argv);

    int myRank;
    MPI_Comm_rank(MPI_COMM_WORLD, &myRank);
    int val[3];
    if(myRank == 0)
    {
        val[0] = 12;
        val[1] = 36;
        val[2] = 18;
    }
    else if(myRank == 1)
    {
        val[0] = 48;
        val[1] = 72;
        val[2] = 44;
    }
    else if(myRank == 2)
    {
        val[0] = 33;
        val[1] = 7;
        val[2] = 47;
    }

    int res[3];
    for(int i = 0; i < 3; ++i)
        res[i] = 0;

    reduce(val, res, 3, 0, 0, MPI_COMM_WORLD);
    if(myRank == 0) {
        printf("Addition of values:\n");
        for(int i = 0; i < 3; ++i) {
            printf("res[%d]: %d\n", i, res[i]);
        }
        printf("\n");
    }

    reduce(val, res, 3, 1, 0, MPI_COMM_WORLD);
    if(myRank == 0) {
        printf("Multiplication of values:\n");
        for(int i = 0; i < 3; ++i) {
            printf("res[%d]: %d\n", i, res[i]);
        }
        printf("\n");
    }

    reduce(val, res, 3, 2, 0, MPI_COMM_WORLD);
    if(myRank == 0) {
        printf("Maximum of values:\n");
        for(int i = 0; i < 3; ++i) {
            printf("res[%d]: %d\n", i, res[i]);
        }
        printf("\n");
    }

    reduce(val, res, 3, 3, 0, MPI_COMM_WORLD);
    if(myRank == 0) {
        printf("Minimum of values:\n");
        for(int i = 0; i < 3; ++i) {
            printf("res[%d]: %d\n", i, res[i]);
        }
        printf("\n");
    }

    if(myRank == 0)
        printf("Measuring time taken:\n");
    MPI_Barrier(MPI_COMM_WORLD);
    double st = MPI_Wtime();
    double time;

    reduce(val, res, 3, 0, 0, MPI_COMM_WORLD);
    reduce(val, res, 3, 1, 0, MPI_COMM_WORLD);
    reduce(val, res, 3, 2, 0, MPI_COMM_WORLD);
    reduce(val, res, 3, 3, 0, MPI_COMM_WORLD);

    double ut = MPI_Wtime()-st;
    MPI_Reduce(&ut, &time, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);
    if(myRank == 0)
        printf("Time taken for own implementation (4 function calls): %f\n", ut);

    st = MPI_Wtime();
    MPI_Reduce(val, res, 3, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
    MPI_Reduce(val, res, 3, MPI_INT, MPI_PROD, 0, MPI_COMM_WORLD);
    MPI_Reduce(val, res, 3, MPI_INT, MPI_MAX, 0, MPI_COMM_WORLD);
    MPI_Reduce(val, res, 3, MPI_INT, MPI_MIN, 0, MPI_COMM_WORLD);

    ut = MPI_Wtime()-st;
    MPI_Reduce(&ut, &time, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);
    if(myRank == 0)
        printf("Time taken for compiler implementation (4 function calls): %f\n", ut);

    MPI_Finalize();

    return 0;
}


