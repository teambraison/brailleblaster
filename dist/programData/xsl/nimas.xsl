<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>

<!-- XSLT for importing NIMAS archive documents into BrailleBlaster -->

<!-- body template -->
<xsl:template match="/">
      <html><body>
         <xsl:apply-templates/>
      </body></html>
</xsl:template>

<!-- document title -->
<xsl:template match="/dtbook/head/meta[@name='dc:Title']">
   <p><xsl:value-of select="@content"/> </p><p/>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="/dtbook/head/meta[@name='dc:Creator']">
   <p><xsl:value-of select="@content"/></p><p/>
   <p>\u00a0</p>
</xsl:template>

<!-- book body -->

<xsl:template match="pagenum">
   <p><xsl:text>- </xsl:text><xsl:value-of select="."/>
   <xsl:text> -</xsl:text></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="h1|h2|h3|h4">
   <p>\u00a0</p>
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="p">
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="img">
   <p>(<xsl:value-of select="attribute::alt"/>)</p>
</xsl:template>

<xsl:template match="prodnote|caption">
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="list">
  <p><xsl:value-of select="hd"/></p>
   <xsl:for-each select="li">
     <p><xsl:value-of select="normalize-space(.)"/></p>
  </xsl:for-each>
  <p>\u00a0</p>
</xsl:template>

<xsl:template match="table">
  <p>\u00a0</p>
  <p><xsl:value-of select="normalize-space(thead)"/></p>
   <xsl:for-each select="tr"><p>
      <xsl:for-each select="td">
       <xsl:value-of select="."/><xsl:text>   </xsl:text>
      </xsl:for-each></p>
   </xsl:for-each>
  <p>\u00a0</p>
</xsl:template>

<xsl:template match="line">
   <p><xsl:value-of select="normalize-space(.)"/></p>
</xsl:template>

<xsl:template match="blockquote">
   <xsl:for-each select="span|p">
     <p><xsl:value-of select="normalize-space(.)"/></p>
     <p>\u00a0</p>
  </xsl:for-each>
</xsl:template>

<xsl:template match="cite">
   <p><xsl:value-of select="."/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="dt">
   <p><xsl:value-of select="normalize-space(.)"/></p>
</xsl:template>

<xsl:template match="dd">
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="sidebar">
   <p>\u00a0</p>
   <p><xsl:value-of select="hd"/></p>
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="frontmatter|rearmatter">
   <p>\u00a0</p>
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

</xsl:stylesheet>