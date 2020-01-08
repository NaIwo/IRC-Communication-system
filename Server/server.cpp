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
}; //struktura opisujaca klienta - zawiera deskryptor, nick, numer pokoju

struct thread_data_t {
    User user;
    pthread_mutex_t room_mutex[6];
    pthread_mutex_t* clients_mutex;
    vector<User>* clients;
}; //struktura przekazywana przy tworzeniu watku

//Funkcja sprawdzajaca czy dany deskryptor klienta istnieje w wektorze klientow
//Jesli nie to zwraca wartosc false, jesli tak to zwraca wartosc true
bool checkIfDescriptorExists(int descriptor, vector<User> *clients, pthread_mutex_t* clients_mutex) {
    pthread_mutex_lock(clients_mutex);
    int size = clients->size();
    for(int i=0; i < size; i++) {
        if ((*clients)[i].descriptor == descriptor) {
            return true;
        }
    }
    pthread_mutex_unlock(clients_mutex);

    struct User user;
    user.descriptor = descriptor;
    user.name = "";
    user.room = 0;
    clients->push_back(user);

    cout << "Dodano nowego klienta o deskryptorze: " << descriptor << endl;
    return false;
}

//Funkcja szczytujaca nick z bufora i sprawdzajaca czy podany nick jest juz zarezerwowany
//Jesli tak zwracana jest wartosc true, jesli nie zwracana jest wartosc false
bool checkIfLoginExists(thread_data_t *th_data) {
    char length[4];
    int pom;

    //Odczytanie dlugosci nicku klienta
    while((pom = read(th_data -> user.descriptor, length, sizeof(length)) == 0)) {

        if(pom == -1) {
            cout << "Nie udało się odczytać dlugosci nicku" << endl;
            return true;
        }
    }

    pom = atoi(length);
    char* nick = new char[pom];

    int read_result;
    read_result = read(th_data -> user.descriptor, nick, pom); //Odczytanie nicku klienta
    if(read_result == -1) {
        cout << "Nie udało się odczytac nicku" << endl;
        return true;
    }

    pthread_mutex_lock(th_data->clients_mutex);
    int size = th_data -> clients -> size();
    int index;

    //Sprawdzenie czy podany nick istnieje juz w wektorze klientow
    for(int i=0; i<size; i++) {

        if(!(*th_data->clients)[i].name.compare(nick)) {
            cout << "Istnieje juz ktos o podanym nicku" << endl;
            pthread_mutex_unlock(th_data->clients_mutex);
            return true;
        }

        if((*th_data->clients)[i].descriptor == th_data -> user.descriptor)
            index = i;
    }
    pthread_mutex_unlock(th_data->clients_mutex);

    cout << "Dodano nick: " << nick << " dla klienta o deskryptorze " << th_data -> user.descriptor << endl;
    th_data -> user.name = nick;
    th_data -> user.room = 0;
    (*th_data->clients)[index].name = nick; //ustawienie nicku danemu klientowi
    return false;
}

//Funkcja pozbywajaca sie zbednych rzeczy z bufora po wykonaniu danej operacji
//Po kazdej wykonanej operacji zostaja w buforze 2 bajty, wiec zostaja one odczytane tak zeby pozostawic pusty bufor
void getRidOfRub(thread_data_t *th_data) {
    char buf[2];
    read(th_data -> user.descriptor, buf, sizeof(buf));
}

//Funkcja usuwajaca klienta o danym deskryptorze z wektora klientow
//Wywolywana po zakonczeniu uzytkowania z aplikacji przez klienta
void removeClientFromClients(thread_data_t *th_data) {
    int size = th_data -> clients -> size();
    for(int i=0; i<size; i++) {
        if(th_data -> user.descriptor == (*th_data->clients)[i].descriptor) {
            th_data->clients->erase(th_data->clients->begin() + i);
            break;
        }
    }
}

//Funkcja uaktualniajaca zmiane pokoju przez danego klienta
//Zwraca stary numer pokoju w celu wyslania tam informacji o opuszczeniu pokoju przez uzytkownika
int changeRoomByClient(thread_data_t *th_data) {
    char room[1];
    int pom;

    //Odczytanie numeru pokoju
    while((pom = read(th_data -> user.descriptor, room, sizeof(room))) == 0) {

        if(pom == -1) {
            cout << "Nie udalo sie odczytac numeru pokoju" << endl;
        }
    }
    
    int old_room = th_data -> user.room;
    th_data -> user.room = atoi(room);
    pthread_mutex_lock(th_data->clients_mutex);
    pom = th_data->clients->size();
    for(int i=0; i<pom; i++) {
        if(th_data -> user.descriptor == (*th_data->clients)[i].descriptor) {
            (*th_data->clients)[i].room = atoi(room); //Ustawienie klientowi nowego numeru pokoju
            break;
        }         
    }
    pthread_mutex_unlock(th_data->clients_mutex);

    cout << "Klient o deskryptorze " << th_data -> user.descriptor << " zmienil pokoj na " << th_data -> user.room << endl;
    return old_room;
}

//Funkcja zwracajaca string z dlugoscia nicku w konwencji czteroznakowej
//dlugosc nicku zostaje dopelniona z lewej strony zerami do czterech znakow
string getLengthOfNick(thread_data_t *th_data) {
    string length;

    if(th_data -> user.name.size() < 10) {length = "000" + to_string(th_data -> user.name.length());}
    else if(th_data -> user.name.size() < 100) {length = "00" + to_string(th_data -> user.name.length());}
    else if(th_data -> user.name.size() < 1000) {length = "0" + to_string(th_data -> user.name.length());}
    else {length = to_string(th_data -> user.name.length());}

    return length;

}

//Funkcja wysylajaca informacje o zmianie pokoju przez danego klienta do okreslonego pokoju
void sendMessageToOthersAboutChangingRoom(thread_data_t *th_data, int room, int mode) {

    string answer;
    if(mode == 2) answer = "2" + getLengthOfNick(th_data) + th_data -> user.name + "\n"; //Wyslanie do starego pokoju
    else answer = "1" + getLengthOfNick(th_data) + th_data -> user.name + "\n"; //Wyslanie do nowego pokoju
    
    char temp[answer.size()];
    strcpy(temp, answer.c_str());
    int size = th_data->clients->size();
    pthread_mutex_lock(&th_data->room_mutex[room]);
    for(int i=0; i< size; i++) {
        if((*th_data->clients)[i].room == room) {
            int write_result = write((*th_data->clients)[i].descriptor, temp, sizeof(temp));
            if(write_result == -1)
                cout << "Nie udało się wyslac wiadomosci odnosnie zmiany pokoju" << endl;
        }
            
    }
    pthread_mutex_unlock(&th_data->room_mutex[room]);
}

//Funkcja pobierajaca dlugosc wiadomosci oraz wiadomosc z bufora
//Funkcja ta przekazuje odczytana wiadomosc dalej do okreslonego pokoju
void getMessegeAndSendItToOthers(thread_data_t *th_data) {
    char length[4];
    int pom;

    //Odczytanie dlugosci wiadomosci uzytkownika
    while((pom = read(th_data -> user.descriptor, length, sizeof(length)) == 0)) {

        if(pom == -1) {
            cout << "Nie udało się odczytać dlugosci wiadomosci" << endl;
        }
    }
    
    pom = atoi(length);
    char* message = new char[pom];


    int read_result;
    read_result = read(th_data -> user.descriptor, message, pom); //Odczytanie wiadomosci od uzytkownika
    if(read_result == -1) {
        cout << "Nie udało się odczytac wiadomosci" << endl;
    }
    
    string s(length);
    string answer = "0" + getLengthOfNick(th_data) + th_data -> user.name + s + message + "\n";
    char temp[answer.size()];
    strcpy(temp, answer.c_str());

    pthread_mutex_lock(&th_data->room_mutex[th_data -> user.room]);
    int size = th_data->clients->size();
    for(int i=0; i< size; i++) {
        //Wyslanie wiadomosci w przyjetej konwencji do okreslonego pokoju
        if((*th_data->clients)[i].room == th_data -> user.room) {
                int write_result = write((*th_data->clients)[i].descriptor, temp, sizeof(temp));
                if(write_result == -1)
                    cout << "Nie udało się wysłać wiadomosci do danego pokoju" << endl;
            }
            
    }
    cout << "Klient o deskryptorze " << th_data -> user.descriptor << " wyslal wiadomosc do pokoju " << th_data -> user.room << endl;
    pthread_mutex_unlock(&th_data->room_mutex[th_data -> user.room]);
}

//Funkcja opisujaca dzialanie watku
void *ThreadBehavior(void *t_data) {
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*) t_data;
    int character;
    
    char mode[1];

    while(1) {
        character = read(th_data -> user.descriptor, mode, sizeof(mode)); //szczytanie operacji do wykonania

        if(character == -1) {
            cout << "Błąd przy odczytywaniu - konczenie watku" << endl;
            break;
        }
        else if (character > 0) {
            if(atoi(mode) == 0) { //Jesli 0 to sprawdzenie mozliwosci nadania nicku i ustawienie go
                if(!checkIfLoginExists(th_data)) {
                    pthread_mutex_lock(&th_data->room_mutex[0]);
                    write(th_data -> user.descriptor, "1\n", 2 * sizeof(char));
                    pthread_mutex_unlock(&th_data->room_mutex[0]);
                }
                else {
                    pthread_mutex_lock(&th_data->room_mutex[0]);
                    write(th_data -> user.descriptor, "0\n", 2 * sizeof(char)); 
                    pthread_mutex_unlock(&th_data->room_mutex[0]);
                    break;} //lub zakonczenie watku
                getRidOfRub(th_data);
            }
            else if (atoi(mode) == 1) { //Jesli 1 to przeslanie wiadomosci do pokoju
                getMessegeAndSendItToOthers(th_data);
                getRidOfRub(th_data);
            }
            else if (atoi(mode) == 2) { //Jesli 2 to wyslanie wiadomosci odnosnie dolaczenia do pokoju i opuszczenia pokoju
                int room = changeRoomByClient(th_data);
                getRidOfRub(th_data);
                sendMessageToOthersAboutChangingRoom(th_data, room, 2);
                sendMessageToOthersAboutChangingRoom(th_data, th_data -> user.room, 1);
            }
            else if (atoi(mode) == 3) { //Jesli 3 to wyslanie wiadomosci odnosnie opuszczenia pokoju
                int room = th_data -> user.room;
                sendMessageToOthersAboutChangingRoom(th_data, room, 2);
                break;
            }
        }
        memset(mode, 0, sizeof(mode));
    }
    cout << "Usunieto klienta o deskryptorze: " << th_data -> user.descriptor << endl;
    pthread_mutex_lock(th_data->clients_mutex);
    pthread_mutex_lock(&th_data->room_mutex[th_data -> user.room]);
    removeClientFromClients(th_data); //Usuniecie klienta z wektora uzytkownikow
    pthread_mutex_unlock(&th_data->room_mutex[th_data -> user.room]);
    pthread_mutex_unlock(th_data->clients_mutex);
    close(th_data -> user.descriptor);
    delete th_data;
    pthread_exit(NULL); //Zakonczenie watku
}

//Funkcja tworzaca nowy watek dla nowo polaczanego klienta
void handleConnection(int connection_socket_descriptor, vector<User>* clients, pthread_mutex_t* clients_mutex, pthread_mutex_t* room_mutex) {
    int create_result = 0;

    pthread_t thread1;

    struct thread_data_t *t_data = new thread_data_t();
    if(!checkIfDescriptorExists(connection_socket_descriptor, clients, clients_mutex)) {
        t_data->user.descriptor = connection_socket_descriptor;
        t_data->clients = clients;
        t_data->clients_mutex = clients_mutex;
        for(int i=0; i<6; i++) {
            t_data->room_mutex[i] = room_mutex[i];
        }
        create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void*) t_data); //Stworzenie nowego watku

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
    //Mutexy rozwiazujace problem wspolbieznego pisania do okreslonego pokoju
    pthread_mutex_t room_mutex[6] = {PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER, PTHREAD_MUTEX_INITIALIZER};
    //Mutex rozwiazujacy problem wspolbieznej modyfikacji wektora clients
    pthread_mutex_t clients_mutex = PTHREAD_MUTEX_INITIALIZER;
    vector<User> clients; //Wektor zawierajacy dane kazdego klienta

    if (argc < 2) {
        cout << "Nie podano portu na którym ma działać serwer\n";
        return 0;
    }

    int port_num = atoi(argv[1]); //Podanie portu, na ktorym serwer ma dzialac z lini polecen

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
        cout << "Błąd przy próbie ustawienia wielkosci kolejki\n";
        return 0;
    }
    while(1) {
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);

        if (connection_socket_descriptor < 0) {
            cout << "Błąd przy próbie utworzenia gniazda dla połączenia klient-server\n";
            return 0;
        }
        handleConnection(connection_socket_descriptor, &clients, &clients_mutex, room_mutex);
    }
    close(server_socket_descriptor);
    return 0;
}


