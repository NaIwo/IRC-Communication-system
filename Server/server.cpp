#include <string.h>
#include <iostream>
#include <vector>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <fstream>
#include <unistd.h>
#include <pthread.h>
#define QUEUE_SIZE 5
using namespace std;


vector<int> clients_descriptors;
struct thread_data_t {
    int descriptor;
};


bool checkIfDescriptorExists(int decriptor) {
    int size = clients_descriptors.size();
    for(int i=0; i < size; i++) {
        if (clients_descriptors[i] == decriptor) {
            return true;
        }
    }
    clients_descriptors.push_back(decriptor);
    return false;
}

void *ThreadBehavior(void *t_data) {
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*) t_data;
    int characters;
    cout << "Utworzono watek" << endl;
    char buf[3];
    string x;
    while(1) {
        characters = read(th_data -> descriptor, buf, 3);
        printf("%s", buf);
        memset(buf, 0, sizeof(buf));
    }
    //close(th_data -> decriptor);
    //delete th_data;
    pthread_exit(NULL);
}

void handleConnection(int connection_socket_descriptor) {
    int create_result = 0;

    pthread_t thread1;

    struct thread_data_t *t_data = new thread_data_t();
    if(!checkIfDescriptorExists(connection_socket_descriptor)) {
        t_data->descriptor = connection_socket_descriptor;
        create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void*) t_data);

        if (create_result) {
        cout << "Nie udało się utworzyć nowego wątku dla klienta\n";
        exit(-1);
        }
    }
}


int main(int argc, char* argv[]) {
    int server_socket_descriptor;
    int connection_socket_descriptor;
    int bind_result;
    int listen_result;
    char reuse_addr_val = 1;
    struct sockaddr_in server_address;

    if (argc < 2) {
        cout << "Nie podano portu na którym ma działać serwer\n";
        return 0;
    }

    int port_num = atoi(argv[1]);

    //inicjalizacja gniazda serwera
    memset(&server_address, 0, sizeof(struct sockaddr));
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = htonl(INADDR_ANY);
    server_address.sin_port = htons(port_num);

    server_socket_descriptor = socket(AF_INET, SOCK_STREAM, 0);

    if (server_socket_descriptor < 0) {
        cout << "Nie udało się utworzyć gniazda serwera\n";
        return 0;
    }
    setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*) &reuse_addr_val, sizeof(reuse_addr_val));

    bind_result = bind(server_socket_descriptor, (struct sockaddr*) &server_address, sizeof(struct sockaddr));
    if (bind_result < 0) {
        cout << "Nie udało się powiązać danego adresu z gniazdem\n";
        return 0;
    }

    listen_result = listen(server_socket_descriptor, QUEUE_SIZE);
    if (listen_result < 0) {
        cout << "Błąd przy nasłuchiwaniu\n";
        return 0;
    }
    while(1) {
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);

        if (connection_socket_descriptor < 0) {
            cout << "Błąd przy próbie utworzenia gniazda dla połączenia klient-server\n";
            return 0;
        }
        handleConnection(connection_socket_descriptor);
    }
    close(server_socket_descriptor);
    return 0;
}


