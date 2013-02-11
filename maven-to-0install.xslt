<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pom="http://maven.apache.org/POM/4.0.0">
  <xsl:param name="repositoryUri" select="'http://repo.maven.apache.org/maven2/'" />
  <xsl:variable name="artifcatUri" select="concat($repositoryUri, //metadata/groupId, '/', //metadata/artifactId, '/')" />
  <xsl:variable name="versions" select="//metadata/versioning/versions/version" />

  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <interface xmlns="http://zero-install.sourceforge.net/2004/injector/interface"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://zero-install.sourceforge.net/2004/injector/interface http://0install.de/schema/injector/interface/interface.xsd">
      <xsl:variable name="latestPom" select="document(concat($artifcatUri, $versions[last()], '/', //metadata/artifactId, '-', $versions[last()], '.pom'))" />
      <name><xsl:value-of select="$latestPom/pom:project/pom:name" /></name>
      <summary><xsl:value-of select="$latestPom/pom:project/pom:description" /></summary>
      <homepage><xsl:value-of select="$latestPom/pom:project/pom:url" /></homepage>

      <group>
        <environment name="CLASSPATH" insert="." mode="append"/>
        <xsl:for-each select="$versions">
          <xsl:variable name="versionUri" select="concat($artifcatUri, ., '/', //metadata/artifactId, '-', .)" />
          <implementation version="{.}" stability="stable" id="{.}">
            <file href="{$versionUri}.jar" />
          </implementation>
        </xsl:for-each>
      </group>
    </interface>
  </xsl:template>
</xsl:stylesheet>
