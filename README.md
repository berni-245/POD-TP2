# TP2 POD - Reclamos Urbanos

## 👋 Introducción

En este trabajo práctico de la materia de Programación de Objetos Distribuidos se buscó aplicar los conceptos de conexiones distribuidas aprendidos a lo largo de la cursada en un proyecto de manejar múltiples clientes que realizan distintas queries utilizando Hazelcast. El contexto del tp es de un programa de consultas acerca de reclamos para distintas ciudades. 

[Enunciado](../pod-tp2/docs/TPE2%20Reclamos%20Urbanos.pdf)

### ❗ Requisitos:
- Java 21
- [Maven](https://maven.apache.org/download.cgi)
- Terminal estilo Unix

Clonar el proyecto utilizando:
```shell
git clone https://github.com/berni-245/POD-TP2.git
```

## 🛠️ Compilación
Desde la terminal y parándose en la carpeta raíz del proyecto correr el siguiente comando:
```shell
mvn clean install
```

## 🏃 Ejecución

### 🌐 Cluster
Una vez se tenga el proyecto compilado, lo primero que queremos hacer es iniciar el cluster. Dirigirse a la carpeta `server/target/` y ahí veremos un archivo `tpe2-g3-server-1.0-SNAPSHOT-bin.tar.gz`. Descomprimirlo usando algún comando como:
```shell
tar -xzf tpe2-g3-server-1.0-SNAPSHOT-bin.tar.gz
```
Y ahora tendremos una carpeta `tpe2-g3-server-1.0-SNAPSHOT`. Accedemos a dicha carpeta y tendremos un archivo `run-server.sh`, le damos permisos de ejecución utilizando:
```shell 
chmod u+x run-server.sh 
```
Luego borramos los retornos de carro `\r` al final de cada línea (esto afecta a ciertas terminales de linux) se puede usar:
```shell 
sed -i 's/\r$//' run-server.sh
```
o usar el editor `vim` sobre el archivo y correr el comando:
```shell 
:set ff=unix
```

Finalmente corremos el cluster con:
```shell 
./run-server.sh 
```
Cada vez que se corra este comando se creará un "nodo", y se listará en terminal todos los nodos conectados al cluster. Guardarse las direcciones conectadas de los nodos pues estarás serán las direcciones que utilizará el cliente.

### 👨🏻‍💼 Cliente 
Ya con el cluster corriendo, vamos a hacer los mismos pasos para los clientes desde otra consola.
Vamos para la carpeta `client/target/`, descomprimimos el archivo `tpe2-g3-client-1.0-SNAPSHOT-bin.tar` y accedemos a la carpeta `tpe2-g3-client-1.0-SNAPSHOT`.
Una vez ahí, corremos el comando:
```shell 
chmod u+x *.sh 
```
Para darle permiso de ejecución a todos los `.sh`. Borramos los retornos de carro utilizando alguna de las dos formas sugeridas en la sección de `Cluster` y finalmente corremos alguno de los clientes con:
```shell 
./<client>.sh <args> 
```
Para especificar un argumento utilizar `-D<arg>=<val>`, con `<arg>` el nombre del argumento y `<val>` su valor. Cada `<Client>` tendrá distintos `<args>`, aunque para todos siempre se deberá especificar ciertos argumentos en común:
* addresses: Para este parámetro hay que usar una o más direcciones con el formato 'xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY;...', estas direcciones deben ser de alguno de los nodos del cluster.
* city: Las iniciales de la ciudad, deben coincidir con las iniciales que estén en los archivos de requests y types
* inPath: directorio donde se encuentran los archivos serviceRequests\<city>.csv y serviceTypes\<city>.csv, es importante que los archivos tengan ese nombre específico
* outPath: directorio dónde se generarán los outputs queryX.csv y timesX.csv

Query 1: 
    
Consulta el total de reclamos por tipo y agencia, ordenados descendente por total de reclamos y desempata alfabético por tipo y luego alfabético por agencia. No requiere argumentos adicionales.

Ejemplo:
```shell 
./query1.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=. -DoutPath=. 
```

Query 2:

Consulta los tipos de reclamos más populares por barrio y cuadrante, ordenados alfabético por barrio y desempata ascendente por latitud del cuadrante y luego ascendente por longitud del cuadrante. Tiene un argumento adicional:
* q: representa los grados utilizados para dividir por las latitudes y longitudes de los reclamos, consiguiendo así un cuadrante de espacio geográfico.

Ejemplo:
```shell 
./query2.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=. -DoutPath=. -Dq=0.1 
```
Query 3:

Consulta la media móvil de reclamos abiertos por agencia, año y mes, ordenados alfabético por agencia y desempata cronológico por año y luego cronológico por mes. Tiene un argumento adicional:
* w: representa la ventana de meses dentro de cada año que se debe usar para calcular la media móvil.

Ejemplo:
```shell 
./query3.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=. -DoutPath=. -Dw=3 
```
Query 4:

Consulta el porcentaje de tipos de reclamo por calle, ordenados descendente por porcentaje y desempata alfabético por calle. Tiene un argumento adicional:
* neighbourhood: el barrio en el cual se verán las calles y sus porcentajes de reclamos

Ejemplo:
```shell 
./query4.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=. -DoutPath=. -Dneighbourhood=MANHATTAN 
```

## 👥 Equipo
- [Padula Morillo, Alejo](https://github.com/AlekDG)
- [Scheffer, Tomás Guillermo](https://github.com/tomaScheffer)
- [Zapico, Bernardo](https://github.com/berni-245)
