<?xml version="1.0" encoding="UTF-8"?>
<!--
  * BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org
  *
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>

<!-- XSLT for importing epub archive documents into BrailleBlaster -->

<!-- body template -->
<xsl:template match="/">
      <html><body>
         <xsl:apply-templates/>
      </body></html>
</xsl:template>

<xsl:template match="title">
   <p><xsl:value-of select="."/></p>
   <p>\u00a0</p>
</xsl:template>

<xsl:template match="p">
   <p><xsl:value-of select="normalize-space(.)"/></p>
   <p>\u00a0</p>
</xsl:template>

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

</xsl:stylesheet>