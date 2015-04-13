<?xml version="1.0"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<profiles>
		<profile>
			<id>dev</id>
			<properties>
				<gitlabPrivateToken>${erel_gitlabPrivateToken}</gitlabPrivateToken>
				<deploy.server.url>http://192.168.0.72:8080/manager</deploy.server.url>
				<deploy.server.id>TomcatServer.integration</deploy.server.id>
				<tomcat.home>/home/nherbaut/Servers/apache-tomcat-6.0.36/</tomcat.home>
				<redmineAPIKey>${erel_redmineAPIKey}</redmineAPIKey>
				<!-- check out this page to get it: http://redmine.erel.net/my/account 
					! -->
				<redmineHost>http://redmine.erel.net</redmineHost>
				<nexusHost>http://repo.erel.net</nexusHost>
				<nexusUser>${erel_nexusUser}</nexusUser>
				<nexusPassword>${erel_nexusPassword}</nexusPassword>
			</properties>
			<repositories>
				<repository>
					<id>central</id>
					<url>http://central</url>
					<releases>
						<enabled>true</enabled>
					</releases>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
				</repository>
			</repositories>
			<pluginRepositories>
				<pluginRepository>
					<id>central</id>
					<url>http://central</url>
					<releases>
						<enabled>true</enabled>
					</releases>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
				</pluginRepository>
			</pluginRepositories>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
		</profile>
	</profiles>
	<activeProfiles>
		<activeProfile>nicolas</activeProfile>
	</activeProfiles>
	<mirrors>
		<mirror>
			<id>nexus</id>
			<url>http://repo.erel.net/content/groups/public/</url>
			<!-- <url>http://nexus.erel.local/nexus/content/groups/public/</url> -->
			<mirrorOf>*</mirrorOf>
		</mirror>
	</mirrors>
	<servers>
		<!-- utilisé pour télécharger les dépendances -->
		<server>
			<id>nexus</id>
			<username>${nexusUser}</username>
			<password>${nexusPassword}</password>
		</server>
		<server>
			<id>ldap</id>
			<username>${nexusUser}</username>
			<password>${nexusPassword}</password>
		</server>

		<!-- utilisé pour uploader les snapshots -->
		<server>
			<id>snapshots</id>
			<username>${nexusUser}</username>
			<password>${nexusPassword}</password>
		</server>
		<!-- utilisé pour uploader des versions release -->
		<server>
			<id>releases</id>
			<username>${nexusUser}</username>
			<password>${nexusPassword}</password>
		</server>
		<!-- utilisé pour pousser une webapp sur un serveur tomcat -->
		<server>
			<id>TomcatServer.integration</id>
			<username>admin</username>
			<password>admin</password>
		</server>
		<server>
			<id>TomcatServer.qualif1</id>
			<username>admin</username>
			<password>admin</password>
		</server>
		<server>
			<id>TomcatServer.qualif2</id>
			<username>admin</username>
			<password>admin</password>
		</server>
	</servers>
	<!-- groups de plugin authorisés -->
	<pluginGroups>
		<pluginGroup>net.erel.maven.plugins</pluginGroup>
		<pluginGroup>org.apache.tomcat.maven</pluginGroup>
		<pluginGroup>org.mortbay.jetty</pluginGroup>
		<pluginGroup>org.jenkins-ci.tools</pluginGroup>
	</pluginGroups>
</settings>