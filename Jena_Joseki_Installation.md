How to install Joseki.

## The steps required to install the SPARQL server for Jena (Joseki) ##


### Steps ###

  1. Download Joseki from **http://www.joseki.org/**.

> 2- Extract the compressed file to a specific folder.

> 3- Add a new environment variable called **JOSEKIROOT** and set its value equal to the path to which you have extracted the zip file.

> 4- Change the **CLASSPATH** environment variable by adding **%JOSEKIROOT%\lib** to the end of its value.

> 5- You may need to restart the computer for changes to take effect.

> 6- You may need also to move subfolders called **webapps** and **Data** to **Bin** subfolder.

> 7- Go to **Bin** subfolder of your installation folder.

> 8- Run the batch file called **rdfserver**.

> 9- The server should be online now and you can access it via **http://localhost:2020**.