#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdio.h>
#include <iostream>
#include <vector>


using namespace std;

#define BUF_SIZE 1024
#define NUM_THREADS 5


struct thread_data_t
{
   int x;
};


void *ThreadBehavior(void *t_data)
{
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;

    char buf[64] = {0};
    memset(buf, 0, sizeof(buf));
   
    while(1)
    {
      int n = read(th_data->x, buf, sizeof(buf));
      buf[n]='\0';

         printf("%s\n",buf);
         memset(buf, 0, sizeof(buf));
      
    } 
    pthread_exit(NULL);
}



void handleConnection(int connection_socket_descriptor) {

    int create_result = 0;

    pthread_t thread1;

     struct thread_data_t t_data;
    t_data.x = connection_socket_descriptor;
    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)&t_data);
    if (create_result){
       printf("Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
       exit(-1);
    }

    char buf[64];
    while(1)
    {
       fgets(buf, sizeof(buf), stdin);
      write(connection_socket_descriptor, buf, strlen(buf));
    }
    
}


int main (int argc, char *argv[])
{
   int connection_socket_descriptor;
   int connect_result;
   struct sockaddr_in server_address;
   struct hostent* server_host_entity = new hostent();

   if (argc != 3)
   {
     fprintf(stderr, "Sposób użycia: %s server_name port_number\n", argv[0]);
     exit(1);
   }



   connection_socket_descriptor = socket(PF_INET, SOCK_STREAM, 0);
   if (connection_socket_descriptor < 0)
   {
      fprintf(stderr, "%s: Błąd przy probie utworzenia gniazda.\n", argv[0]);
      exit(1);
   }

   memset(&server_address, 0, sizeof(struct sockaddr));
   server_address.sin_family = AF_INET;
   memcpy(&server_address.sin_addr.s_addr, &server_host_entity->h_addr_list[0], server_host_entity->h_length);
   server_address.sin_port = htons(atoi(argv[2]));
 
   connect_result = connect(connection_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
   if (connect_result < 0)
   {
      fprintf(stderr, "%s: Błąd przy próbie połączenia z serwerem (%s:%i).\n", argv[0], argv[1], atoi(argv[2]));
      exit(1);
   }

   handleConnection(connection_socket_descriptor);

   close(connection_socket_descriptor);
   return 0;

}
