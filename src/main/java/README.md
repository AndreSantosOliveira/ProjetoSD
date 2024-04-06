# Projeto de Sistemas Distribuídos
#### Meta 1 - LEI FCTUC 2024
José Rodrigues (2021235353) e André Oliveira (2021226714)

# Setup

1. Compilação dos ficheiros .java como Javac
2. Editar os property files / configuration files:
   
    i.  accounts.txt -> ficheiro de configuração das contas presentes na gateway (0 para uma conta normal, 1 para conta de administrador)
   
    ii. barrels.txt -> ficheiro de configuração dos barrels no formato ip:porta:ipExterno, caso um barrel tente aceder a outro que não esteja localmente na máquina.
  
    iii. downloaders.txt -> ficheiro de configuração dos downloaders no formato ip:porta
   
    iv. connections.txt ->  ficheiro de configuração das ligações da Gateway, QueueManger, BarrelManager, DownloadManager e Multicast no formato: componente|ip:porta

## Executar o Programa

Depois de definidos os dados nos ficheiros de configuração, executar o BarrelManager, Barrel, DownloadManager, Downloader, QueueManager, Gateway, ClienteRMI.