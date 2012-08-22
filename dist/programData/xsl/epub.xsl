<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>

<!-- body template -->
<xsl:template match="/">
      <html><body>
         <xsl:apply-templates/>
      </body></html>
</xsl:template>

<xsl:template match="html/head/title">
   <p><xsl:value-of select="."/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="p">
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

<!-- 

<xsl:template match="h1|h2|h3|h4">
   <p>\u00a0</p>
   <p><xsl:value-of select="."/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="list">
  <p><xsl:value-of select="./hd"/></p>
   <xsl:for-each select="li">
     <p><xsl:value-of select="normalize-space(.)"/></p>
  </xsl:for-each>
  <p>\u00a0</p>
</xsl:template>

<xsl:template match="table">
  <p><xsl:value-of select="normalize-space(thead)"/></p>
  <p>\u00a0</p>
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

<xsl:template match="cite">
   <p>\u00a0</p>
   <p><xsl:value-of select="."/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="dt">
   <p>\u00a0</p>
   <p><xsl:value-of select="normalize-space(.)"/></p>
</xsl:template>

<xsl:template match="dd">
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="img">
   <p>(<xsl:value-of select="attribute::alt"/>)</p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="prodnote|caption">
   <p>\u00a0</p>
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>


<xsl:template match="frontmatter|rearmatter">
   <p>\u00a0</p>
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>
-->
</xsl:stylesheet>