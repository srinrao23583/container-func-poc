ARG JAVA_VERSION=17		 
	
#FROM mcr.microsoft.com/playwright/java:v1.30.0-focal

#RUN apt-get install libglib2.0-0 libnss3 libnspr4 libdbus-1-3 libatk1.0-0 libatk-bridge2.0-0 libatspi2.0-0 libxcomposite1 libxdamage1 libxext6 libxfixes3 libxrandr2 libgbm1 libdrm2 libxkbcommon0 libasound2
# This image additionally contains function core tools – useful when using custom extensions
#FROM mcr.microsoft.com/azure-functions/java:4-java$JAVA_VERSION-core-tools AS installer-env
#Commented by Srini
FROM mcr.microsoft.com/azure-functions/java:4-java$JAVA_VERSION-build AS installer-env
#FROM mcr.microsoft.com/playwright/java:v1.30.0-focal

# Set the working directory
#WORKDIR /app
# Copy the project files to the container
#COPY . .

COPY . /src/java-function-app
RUN cd /src/java-function-app && \
    mkdir -p /home/site/wwwroot && \
    mvn clean package && \
    cd ./target/azure-functions/ && \
    cd $(ls -d */|head -n 1) && \
    cp -a . /home/site/wwwroot

# This image is ssh enabled
#Commented by Srini
FROM mcr.microsoft.com/azure-functions/java:4-java$JAVA_VERSION-appservice
# This image isn't ssh enabled
#FROM mcr.microsoft.com/azure-functions/java:4-java$JAVA_VERSION
RUN apt-get update && \
    apt-get install -y gconf-service libasound2 libatk1.0-0 libatk-bridge2.0-0 libc6 \
    libcairo2 libcups2 libdbus-1-3 libexpat1 libfontconfig1 libgcc1 libgconf-2-4 \
    libgdk-pixbuf2.0-0 libglib2.0-0 libgtk-3-0 libnspr4 libpango-1.0-0 libpangocairo-1.0-0 \
    libstdc++6 libx11-6 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxdamage1 libxext6 \
    libxfixes3 libxi6 libxrandr2 libxrender1 libxss1 libxtst6 ca-certificates fonts-liberation \
    libappindicator1 libnss3 lsb-release xdg-utils wget \
    # For (newer) Chromium
    libgbm1

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true
# Install Playwright and its dependencies
#RUN mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
	
COPY --from=installer-env ["/home/site/wwwroot", "/home/site/wwwroot"]