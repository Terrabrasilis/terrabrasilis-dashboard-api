# Deforestation Data Feeder

Updated guide to run the project locally (without using Docker).

## Requirements

Before starting, make sure you have the following packages installed:

- **OpenJDK** version `1.8.0_442`
- **Apache Maven** version `3.3.9`

## Installing OpenJDK

Run the following commands to install OpenJDK 8:

```bash
sudo apt update
apt-cache search openjdk-8
sudo apt install openjdk-8-jdk -y
```

### Verify the installation

To confirm that OpenJDK has been installed correctly, run:

```bash
java -version
```

If the output includes `openjdk version "1.8.0_442"`, the installation was successful!

---

## Installing Maven

Maven `3.3.9` is not available in Ubuntu's standard repositories, so we need to install it manually.

### 1. Download Maven `3.3.9`

```bash
wget https://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
```

### 2. Extract the file to `/opt/`

```bash
sudo tar -xvzf apache-maven-3.3.9-bin.tar.gz -C /opt/
sudo mv /opt/apache-maven-3.3.9 /opt/maven
```

### 3. Configure environment variables

```bash
echo "export M2_HOME=/opt/maven" | sudo tee -a /etc/profile
echo "export PATH=\$M2_HOME/bin:\$PATH" | sudo tee -a /etc/profile
```

### 4. Update environment variables

```bash
source /etc/profile
```

### Verify the installation

To check if Maven was installed correctly, run:

```bash
mvn -version
```

If the output is similar to:

```text
Apache Maven 3.3.9 (bbd5164438bfcab1c3c3c92298472df3de3c413a; 2015-11-10T08:41:47+00:00)
Maven home: /opt/maven
Java version: 1.8.0_222, vendor: AdoptOpenJDK
```

Then the installation was successful!

---

## Database Configuration

Before proceeding, ensure that the database is set up and running.

To configure the database connection in this project, edit the file:

```text
data-feeder/src/main/resources/application-prod.properties
```

And adjust the following parameters according to your database configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dashboard-data-model
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## Configuring the Directory for Output Files

In the file:

```text
data-feeder/src/main/java/info/terrabrasilis/redis/feeder/util/Constants.java
```

Change the variable `JSON_BASE_PATH` to the directory where you want to save the output files.

Example:

```java
public static final String JSON_BASE_PATH = "/home/user/deforestation-data-feeder/files/output";
```

---

Now the project is ready to run! 🚀

To run the project, navigate to the `data-feeder` directory and execute the command:

```
mvn spring-boot:run
```

