
# Tutorial de Git

Para los que no saben usar *Git* les dejo un pequeño tutorial de los comandos basicos para poder usar el repositorio de *Github*.

## Instalacion
Pueden descargar Git desde [este link](https://git-scm.com/downloads).
una vez lo descargan lo instalan como cualquier programa (dejen todo como esta por defecto).

## Comandos

Para usar Git primero se tienen que posicionar en consola donde quieran hacer los cambios (recuerden usar cd y ls/list(dependiendo del SO) para cambiar de directorio y listar los archivos del directorio actual).
En este ejemplo estamos en la carpeta de documentos, donde queremos tener el repositorio.

### Clonar repositorio
para clonar el repositorio desde Github a una carpeta en su PC se utiliza el siguiente comando:
```git
git clone [url del repositorio]
```
En nuestro caso el url del proyecto es https://github.com/PurpleBnnuyStupid/ProyectoBerea.git asi que pegan eso y les descargara el repositorio a una carpeta (dentro de documentos para el ejemplo).

**Atencion**
Es probable que les pida iniciar sesion para clonar el repositorio, inician sesion con su github con acceso al repositorio y todo deberia funcionar sin problemas.

### Subir cambios
Una vez clonan el repositorio a su computadora hacen los cambios que quieran subiendo sus archivos y cuando esten listos para subirlos posicionan su consola en la raiz del repositorio en su computadora y escriben los siguientes comandos:
```git
git add .
```
Esto agregara todo el directorio junto con los subdirectorios al commit del siguiente comando, tambien pueden agregar archivos individuales.
```git
git commit -m "Mensaje de commit"
```
Con esto generan un commit local con un mensaje que de preferencia describe los cambios que realizaron al proyecto, tales como "clase tal modificada", "arregle bug en X.java donde Y cosa no funcionaba" y mensajes por el estilo.
```git
git push origin master
```
Con este comando suben su commit a la rama master del repositorio en Github. es la unica rama que vamos a usar asi que siempre usen master aca.

### Actualizar repositorio local 
Para actualizar el repositorio que clonaron con los ultimos cambios en el repositorio de Github se posicionan dentro del repositorio en la consola y tipean este comando:

```git
git pull origin master
```
