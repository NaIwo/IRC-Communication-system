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

struct User {
    int descriptor;
    string name;
    int room;
};

struct thread_data_t {
    User user;
};



vector<User> clients;


bool checkIfDescriptorExists(int descriptor) {
    int size = clients.size();
    for(int i=0; i < size; i++) {
        if (clients[i].descriptor == descriptor) {
            return true;
        }
    }
    struct User user;
    user.descriptor = descriptor;
    user.name = "";
    user.room = 0;
    clients.push_back(user);
    cout << "Dodano nowego klienta o deskryptorze: " << descriptor << endl;
    return false;
}

bool checkIfLoginExists(thread_data_t *th_data) {
    char length[4];
    int pom;

    while((pom = read(th_data -> user.descriptor, length, sizeof(length)) == 0)) {

        if(pom == -1) {
            cout << "Nie udało się odczytać dlugosci nicku" << endl;
            return true;
        }
    }

    pom = atoi(length);
    char* nick = new char[pom];

    int read_result;
    read_result = read(th_data -> user.descriptor, nick, pom);
    if(read_result == -1) {
        cout << "Nie udało się odczytac nicku" << endl;
        return true;
    }

    int size = clients.size();
    int index;
    for(int i=0; i<size; i++) {
        
        if(!clients[i].name.compare(nick)) {
            cout << "Istnieje juz ktos o podanym nicku" << endl;
            return true;
        }

        if(clients[i].descriptor == th_data -> user.descriptor)
            index = i;
    }

    cout << "Dodano nick: " << nick << " dla klienta o deskryptorze " << th_data -> user.descriptor << endl;
    th_data -> user.name = nick;
    th_data -> user.room = 0;
    clients[index].name = nick;
    return false;
}

void getRidOfRub(thread_data_t *th_data) {
    char buf[100];
    read(th_data -> user.descriptor, buf, sizeof(buf));
}

void removeClientFromClients(thread_data_t *th_data) {
    int size = clients.size();
    for(int i=0; i<size; i++) {
        if(th_data -> user.descriptor == clients[i].descriptor) {
            clients.erase(clients.begin() + i);
            break;
        }
    }
}

int changeRoomByClient(thread_data_t *th_data) {
    char room[1];
    int pom;
    while((pom = read(th_data -> user.descriptor, room, sizeof(room))) == 0) {

        if(pom == -1) {
            cout << "Nie udalo sie odczytac numeru pokoju" << endl;
        }
    }
    
    int old_room = th_data -> user.room;
    th_data -> user.room = atoi(room);
    pom = clients.size();
    for(int i=0; i<pom; i++) {
        if(th_data -> user.descriptor == clients[i].descriptor) {
            clients[i].room = atoi(room);
            break;
        }         
    }

    cout << "Klient o deskryptorze " << th_data -> user.descriptor << " zmienil pokoj na " << th_data -> user.room << endl;
    return old_room;
}


string getLengthOfNick(thread_data_t *th_data) {
    string length;

    if(th_data -> user.name.size() < 10) {length = "000" + to_string(th_data -> user.name.length());}
    else if(th_data -> user.name.size() < 100) {length = "00" + to_string(th_data -> user.name.length());}
    else if(th_data -> user.name.size() < 1000) {length = "0" + to_string(th_data -> user.name.length());}
    else {length = to_string(th_data -> user.name.length());}

    return length;

}

void sendMessageToOthersAboutChangingRoom(thread_data_t *th_data, int room, int mode) {

    string answer;
    if(mode == 2) answer = "2" + getLengthOfNick(th_data) + th_data -> user.name + "\n";
    else answer = "1" + getLengthOfNick(th_data) + th_data -> user.name + "\n";
    
    char temp[answer.size()];
    strcpy(temp, answer.c_str());
    int size = clients.size();
    for(int i=0; i< size; i++) {
        if(clients[i].room == room)
            write(clients[i].descriptor, temp, sizeof(temp));
    }
}

void getMessegeAndSendItToOthers(thread_data_t *th_data) {
    char length[4];
    int pom;

    while((pom = read(th_data -> user.descriptor, length, sizeof(length)) == 0)) {

        if(pom == -1) {
            cout << "Nie udało się odczytać dlugosci nicku" << endl;
        }
    }
    
    pom = atoi(length);
    char* message = new char[pom];


    int read_result;
    read_result = read(th_data -> user.descriptor, message, pom);
    if(read_result == -1) {
        cout << "Nie udało się odczytac nicku" << endl;
    }
    
    string s(length);
    string answer = "0" + getLengthOfNick(th_data) + th_data -> user.name + s + message + "\n";
    char temp[answer.size()];
    strcpy(temp, answer.c_str());

    int size = clients.size();
    for(int i=0; i< size; i++) {
        if(clients[i].room == th_data -> user.room)
            write(clients[i].descriptor, temp, sizeof(temp));
    }
    cout << "Klient o deskryptorze " << th_data -> user.descriptor << " wyslal wiadomosc do pokoju " << th_data -> user.room << endl;
}

void *ThreadBehavior(void *t_data) {
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*) t_data;
    int character;
    
    char mode[1];

    while(1) {
        character = read(th_data -> user.descriptor, mode, sizeof(mode));

        if(character == -1) {
            cout << "Błąd przy odczytywaniu - konczenie watku" << endl;
            break;
        }
        else if (character > 0) {
            if(atoi(mode) == 0) {
                if(!checkIfLoginExists(th_data)) write(th_data -> user.descriptor, "1\n", 2 * sizeof(char));
                else {write(th_data -> user.descriptor, "0\n", 2 * sizeof(char)); break;}
                getRidOfRub(th_data);
            }
            else if (atoi(mode) == 1) {
                getMessegeAndSendItToOthers(th_data);
                getRidOfRub(th_data);
            }
            else if (atoi(mode) == 2) {
                int room = changeRoomByClient(th_data);
                getRidOfRub(th_data);
                sendMessageToOthersAboutChangingRoom(th_data, room, 2);
                sendMessageToOthersAboutChangingRoom(th_data, th_data -> user.room, 1);
            }
            else if (atoi(mode) == 3) {
                int room = th_data -> user.room;
                sendMessageToOthersAboutChangingRoom(th_data, room, 2);
                break;
            }
        }
        memset(mode, 0, sizeof(mode));
    }
    cout << "Usunieto klienta o deskryptorze: " << th_data -> user.descriptor << endl;
    removeClientFromClients(th_data);
    close(th_data -> user.descriptor);
    delete th_data;
    pthread_exit(NULL);
}

void handleConnection(int connection_socket_descriptor) {
    int create_result = 0;

    pthread_t thread1;

    struct thread_data_t *t_data = new thread_data_t();
    if(!checkIfDescriptorExists(connection_socket_descriptor)) {
        t_data->user.descriptor = connection_socket_descriptor;
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


