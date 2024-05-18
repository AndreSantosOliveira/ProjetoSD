# Projeto de Sistemas Distribuídos

Projeto realizado no ãmbito da cadeira de Sistemas Distribuidos na Faculdade de Ciências e Tecnologias da Universidade
de Coimbra (FCTUC).

#### Meta 2 - LEI FCTUC 2024

André Oliveira  (2021226714)<br/>José Rodrigues (2021235353)

# Setup

1. Compilação dos ficheiros .java como Javac


2. Editar os property files / configuration files:

   i. accounts.txt -> ficheiro de configuração das contas presentes na gateway (0 para uma conta normal, 1 para conta de
   administrador)

   ii. barrels.txt -> ficheiro de configuração dos barrels no formato ip:porta:ipExterno, caso um barrel tente aceder a
   outro que não esteja localmente na máquina.

   iii. downloaders.txt -> ficheiro de configuração dos downloaders no formato ip:porta

   iv. connections.txt ->  ficheiro de configuração das ligações da Gateway, QueueManger, BarrelManager, DownloadManager
   e Multicast no formato: componente|ip:porta

Nota: Todos os ficheiros de configuração devem estar na pasta src/main/java/

# Executar o Programa

Depois de definidos os dados nos ficheiros de configuração, executar o BarrelManager, Barrel(s), DownloadManager,
Downloader(s), QueueManager, Gateway, ClienteRMI(s) e WebServer com as portas respetivas definidas nos ficheiros de
configuração.

# URLs Web disponíveis

- http://localhost:8080/ -> Página inicial
- http://localhost:8080/search?query="yourSearchHere" -> Lista de resultados de pesquisa, onde "yourSearchHere" é a
  pesquisa pretendida
- http://127.0.0.1:8080/login -> Página de login
- http://127.0.0.1:8080/account -> Página de conta
- http://127.0.0.1:8080/admin -> Página de administração
- http://127.0.0.1:8080/gato -> Página de gatos
- http://127.0.0.1:8080/weather?city="yourLocationHere" -> Página de meteorologia, onde "yourLocationHere" é a
  cidade/pais pretendida
