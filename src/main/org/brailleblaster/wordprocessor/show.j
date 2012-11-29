private void show (Element node) {
if (node == null) return;
Node newNode;
int branchCount;
  boolean haveStyle = null;
  semanticEntry curSem = null;
  String semanticsTableIndex = node.getAttributeValue ("bbsem");
if (semanticsTableIndex != null) {
curSem = semanticsTable [Integer.parseInt (semanticTableIndex)];
if (curSem.action != null) {
switch (curSem.action) {
case brl:
showBrl (node);
return;
default:
break;
}
}
}
haveStyle = startStyle (curSem.style, node);
for (branchCount = 0; branchCount < node.getChildCount(); branchCount++) 
{
newNode = node.getChild(branchCount);
if (newNode instanceof Element) {
Element element = (Element)newNode;
show (element);
} else if (newNode instanceof Text) {
Text text = (Text)newNode;
handleText (text, curSem);
} else if (newNode instanceof CDATASection) {
CDATASection section = (CDATASection)newNode;
handleCDATASection (section, curSem);
}
}
if (curSem != null) {
switch (curSem.action) {
default:
break;
}
if (haveStyle) endStyle();
}
}
}

private void handleText (Text node, SemanticEntry curSem) {
}

private void handleCDATASection (CDATASection node, SemanticEntry 
curSem) {
}

