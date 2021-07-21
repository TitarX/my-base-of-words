/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainForm.java
 *
 * Created on Dec 1, 2011, 1:44:29 PM
 */
package mybaseofwords;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author TitarX
 */
public class MainForm extends javax.swing.JFrame
{

    private boolean existBaseFile=false;
    private boolean existWord=false;
    private Document baseFileDocument=null;
    private Element baseElement=null;
    private XPathExpression wordXPathExpression=null;
    private NodeList wordNodeList=null;
    private XPathExpression meaningXPathExpression=null;
    private NodeList meaningNodeList=null;
    private Transformer baseFiletransformer=null;
    private StreamResult baseFileStreamResult=null;
    private ArrayList<String> wordsFromBase=new ArrayList<String>();

    /** Creates new form MainForm */
    public MainForm()
    {
        initComponents();
        myInitComponents();
    }

    private void myInitComponents()
    {
        hintsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        File configFile=new File("config.xml");
        if(configFile.exists())
        {
            try
            {
                Document configFileDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
                Element rootElement=configFileDocument.getDocumentElement();
                NodeList mainformNodeList=rootElement.getElementsByTagName("mainform").item(0).getChildNodes();
                Node maximizedNode=getNodeByName(mainformNodeList,"maximized");
                String maximizedValue=null;
                if(maximizedNode!=null)
                {
                    maximizedValue=maximizedNode.getTextContent().trim();
                }
                if(maximizedValue!=null&&maximizedValue.equals("1"))
                {
                    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
                else
                {
                    Node sizeNode=getNodeByName(mainformNodeList,"size");
                    Node locationNode=getNodeByName(mainformNodeList,"location");

                    if(sizeNode!=null&&locationNode!=null)
                    {
                        Node xNode=getNodeByName(locationNode.getChildNodes(),"x");
                        Node yNode=getNodeByName(locationNode.getChildNodes(),"y");
                        Node wNode=getNodeByName(sizeNode.getChildNodes(),"w");
                        Node hNode=getNodeByName(sizeNode.getChildNodes(),"h");

                        if(xNode!=null&&yNode!=null&&wNode!=null&&hNode!=null)
                        {
                            String xValue=xNode.getTextContent().trim();
                            String yValue=yNode.getTextContent().trim();
                            String wValue=wNode.getTextContent().trim();
                            String hValue=hNode.getTextContent().trim();

                            String numberRegex="-?([1-9][0-9]*)|0";
                            String numberRegexWithoutZero="[1-9][0-9]*";
                            if(xValue.matches(numberRegex)&&yValue.matches(numberRegex)
                                    &&wValue.matches(numberRegexWithoutZero)&&hValue.matches(numberRegexWithoutZero))
                            {
                                this.setSize(Integer.parseInt(wValue),Integer.parseInt(hValue));
                                this.setLocation(Integer.parseInt(xValue),Integer.parseInt(yValue));
                            }
                            else
                            {
                                this.setSize(800,600);
                                this.setLocationRelativeTo(null);
                            }
                        }
                        else
                        {
                            this.setSize(800,600);
                            this.setLocationRelativeTo(null);
                        }
                    }
                    else
                    {
                        this.setSize(800,600);
                        this.setLocationRelativeTo(null);
                    }
                }
            }
            catch(Exception ex)
            {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE,null,ex);

                JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
//                JOptionPane.showMessageDialog(this,"<html><font color='red'>"+ex.getMessage()+"</font></html>","Error",JOptionPane.ERROR_MESSAGE);

                this.setSize(800,600);
                this.setLocationRelativeTo(null);
            }
        }
        else
        {
            this.setSize(800,600);
            this.setLocationRelativeTo(null);
        }

        try
        {
            existBaseFile=new File("base.xml").exists();

            wordXPathExpression=XPathFactory.newInstance().newXPath().compile("/base/item/word/text()");
            meaningXPathExpression=XPathFactory.newInstance().newXPath().compile("/base/item/meaning/text()");

            baseFiletransformer=TransformerFactory.newInstance().newTransformer();
            baseFiletransformer.setOutputProperty(OutputKeys.INDENT,"yes");
            baseFiletransformer.setOutputProperty(OutputKeys.METHOD,"xml");
            baseFiletransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
            baseFileStreamResult=new StreamResult(new File("base.xml"));

            if(existBaseFile)
            {
                baseFileDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("base.xml");
                baseElement=baseFileDocument.getDocumentElement();

                wordNodeList=(NodeList)wordXPathExpression.evaluate(baseFileDocument,XPathConstants.NODESET);
                meaningNodeList=(NodeList)meaningXPathExpression.evaluate(baseFileDocument,XPathConstants.NODESET);
            }
            else
            {
                baseFileDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                baseElement=baseFileDocument.createElement("base");
                baseFileDocument.appendChild(baseElement);

            }
        }
        catch(Exception ex)
        {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE,null,ex);
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }

        wordTextField.getDocument().addDocumentListener(new DocumentListener()
        {

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                searchWord();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                searchWord();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                searchWord();
            }
        });

        meaningTextArea.getDocument().addDocumentListener(new DocumentListener()
        {

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                searchSimilars();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                searchSimilars();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                searchSimilars();
            }
        });
    }

    private Node getNodeByName(NodeList nodeList,String nodeName)
    {
        for(int i=0;i<nodeList.getLength();i++)
        {
            Node node=nodeList.item(i);
            if(node.getNodeName().trim().equalsIgnoreCase(nodeName))
            {
                return node;
            }
        }
        return null;
    }

    private void definitionPossibility()
    {
        if(existBaseFile)
        {
            if(wordTextField.getText().trim().equals("")||meaningTextArea.getText().trim().equals(""))
            {
                addButton.setEnabled(false);
                changeButton.setEnabled(false);
                if(!wordTextField.getText().trim().equals("")&&existWord)
                {
                    deleteButton.setEnabled(true);
                }
                else
                {
                    deleteButton.setEnabled(false);
                }
            }
            else
            {
                if(existWord)
                {
                    addButton.setEnabled(false);
                    changeButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                }
                else
                {
                    addButton.setEnabled(true);
                    changeButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            }
        }
        else
        {
            if(wordTextField.getText().trim().equals("")||meaningTextArea.getText().trim().equals(""))
            {
                addButton.setEnabled(false);
                changeButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
            else
            {
                addButton.setEnabled(true);
                changeButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
        }
    }

    private void searchWord()
    {
        if(!wordTextField.getText().trim().equals("")&&existBaseFile)
        {
            int wordIndex=-1;
            wordsFromBase.clear();
            String word=wordTextField.getText().trim();
            for(int i=0;i<wordNodeList.getLength();i++)
            {
                Pattern wordRegex=Pattern.compile("^"+word);
                String wordFromBase=wordNodeList.item(i).getTextContent().trim();
                if(word.equalsIgnoreCase(wordFromBase))
                {
                    wordIndex=i;
                }
                if(wordRegex.matcher(wordFromBase).find()&&!word.equalsIgnoreCase(wordFromBase))
                {
                    wordsFromBase.add(wordFromBase);
                }
            }
            if(!wordsFromBase.isEmpty()&&!word.equals(""))
            {
                Vector<String> wordsFromBaseVector=new Vector<String>();
                wordsFromBaseVector.addAll(wordsFromBase);
                hintsList.setListData(wordsFromBaseVector);
            }
            else
            {
                Vector<String> wordsFromBaseVector=new Vector<String>();
                hintsList.setListData(wordsFromBaseVector);
            }

            if(wordIndex>-1)
            {
                meaningTextArea.setText(meaningNodeList.item(wordIndex).getTextContent().trim());
                meaningTextArea.setForeground(Color.BLACK);
                existWord=true;
            }
            else
            {
                meaningTextArea.setForeground(Color.LIGHT_GRAY);
                existWord=false;
            }
        }
        else
        {
            Vector<String> wordsFromBaseVector=new Vector<String>();
            hintsList.setListData(wordsFromBaseVector);
        }
        definitionPossibility();
    }

    private void searchSimilars()
    {
        meaningTextArea.setForeground(Color.RED);

        if(!meaningTextArea.getText().trim().equals("")&&existBaseFile)
        {
            wordsFromBase.clear();
            String meaning=meaningTextArea.getText().trim();
            for(int i=0;i<meaningNodeList.getLength();i++)
            {
                Pattern meaningRegex=Pattern.compile(meaning);
                String meaningFromBase=meaningNodeList.item(i).getTextContent().trim();
                String word=wordTextField.getText().trim();
                String wordFromBase=wordNodeList.item(i).getTextContent().trim();
                if(meaningRegex.matcher(meaningFromBase).find()&&!word.equalsIgnoreCase(wordFromBase))
                {
                    wordsFromBase.add(wordFromBase);
                }
            }
            if(!wordsFromBase.isEmpty()&&!meaning.equals(""))
            {
                Vector<String> wordsFromBaseVector=new Vector<String>();
                wordsFromBaseVector.addAll(wordsFromBase);
                similarsList.setListData(wordsFromBaseVector);
            }
            else
            {
                Vector<String> wordsFromBaseVector=new Vector<String>();
                similarsList.setListData(wordsFromBaseVector);
            }
        }
        else
        {
            Vector<String> wordsFromBaseVector=new Vector<String>();
            similarsList.setListData(wordsFromBaseVector);
        }
        definitionPossibility();
    }

    private void saveConfig()
    {
        try
        {
            Document configFileDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element configElement=configFileDocument.createElement("config");
            configFileDocument.appendChild(configElement);

            Element mainformElement=configFileDocument.createElement("mainform");
            configElement.appendChild(mainformElement);

            Element maximizedElement=configFileDocument.createElement("maximized");
            if(this.getExtendedState()==JFrame.MAXIMIZED_BOTH)
            {
                maximizedElement.appendChild(configFileDocument.createTextNode("1"));
            }
            else
            {
                maximizedElement.appendChild(configFileDocument.createTextNode("-1"));
            }
            mainformElement.appendChild(maximizedElement);

            Element sizeElement=configFileDocument.createElement("size");
            Element wElement=configFileDocument.createElement("w");
            wElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)this.getSize().getWidth())));
            Element hElement=configFileDocument.createElement("h");
            hElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)this.getSize().getHeight())));
            sizeElement.appendChild(wElement);
            sizeElement.appendChild(hElement);
            mainformElement.appendChild(sizeElement);

            Element locationElement=configFileDocument.createElement("location");
            Element xElement=configFileDocument.createElement("x");
            xElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)this.getLocation().getX())));
            Element yElement=configFileDocument.createElement("y");
            yElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)this.getLocation().getY())));
            locationElement.appendChild(xElement);
            locationElement.appendChild(yElement);
            mainformElement.appendChild(locationElement);

            Transformer configFileTransformer=TransformerFactory.newInstance().newTransformer();
            configFileTransformer.setOutputProperty(OutputKeys.INDENT,"yes");
            configFileTransformer.setOutputProperty(OutputKeys.METHOD,"xml");
            configFileTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
            DOMSource configFileDOMSource=new DOMSource(configFileDocument);
            StreamResult configFileStreamResult=new StreamResult(new File("config.xml"));
            configFileTransformer.transform(configFileDOMSource,configFileStreamResult);
        }
        catch(Exception ex)
        {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE,null,ex);
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addWord()
    {
        try
        {
            Element itemElement=baseFileDocument.createElement("item");
            baseElement.appendChild(itemElement);

            Element wordElement=baseFileDocument.createElement("word");
            wordElement.appendChild(baseFileDocument.createTextNode(wordTextField.getText().trim()));
            itemElement.appendChild(wordElement);

            Element meaningElement=baseFileDocument.createElement("meaning");
            meaningElement.appendChild(baseFileDocument.createTextNode(meaningTextArea.getText().trim()));
            itemElement.appendChild(meaningElement);

            DOMSource baseFileDOMSource=new DOMSource(baseFileDocument);
            baseFiletransformer.transform(baseFileDOMSource,baseFileStreamResult);

            wordNodeList=(NodeList)wordXPathExpression.evaluate(baseFileDocument,XPathConstants.NODESET);
            meaningNodeList=(NodeList)meaningXPathExpression.evaluate(baseFileDocument,XPathConstants.NODESET);

            existBaseFile=true;
            addButton.setEnabled(false);
            changeButton.setEnabled(true);
            deleteButton.setEnabled(true);
            searchWord();
        }
        catch(Exception ex)
        {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE,null,ex);
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }

    }

    private void actionOnWord(String action)
    {
        try
        {
            String word=wordTextField.getText().trim();
            NodeList itemNodeList=baseElement.getElementsByTagName("item");
            for(int i=0;i<itemNodeList.getLength();i++)
            {
                Element element=(Element)itemNodeList.item(i);
                String text=element.getElementsByTagName("word").item(0).getTextContent();
                if(word.equalsIgnoreCase(text))
                {
                    if(action.equalsIgnoreCase("delete"))
                    {
                        baseElement.removeChild(element);

                        rebuildBaseFileDocument(itemNodeList);
                    }
                    else if(action.equalsIgnoreCase("change"))
                    {
                        element.getElementsByTagName("meaning").item(0).setTextContent(meaningTextArea.getText().trim());
                    }
                    break;
                }
            }

            DOMSource baseFileDOMSource=new DOMSource(baseFileDocument);
            baseFiletransformer.transform(baseFileDOMSource,baseFileStreamResult);

            wordNodeList=(NodeList)wordXPathExpression.evaluate(baseFileDocument,XPathConstants.NODESET);
            meaningNodeList=(NodeList)meaningXPathExpression.evaluate(baseFileDocument,XPathConstants.NODESET);

            searchWord();
        }
        catch(Exception ex)
        {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE,null,ex);
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rebuildBaseFileDocument(NodeList itemNodeList) throws Exception
    {
        baseFileDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        baseElement=baseFileDocument.createElement("base");
        baseFileDocument.appendChild(baseElement);
        for(int j=0;j<itemNodeList.getLength();j++)
        {
            Node node=itemNodeList.item(j);
            --j;
            baseFileDocument.adoptNode(node);
            baseElement.appendChild(node);
        }
    }

    private void applySelectedItem(InputEvent evt)
    {
        Object selectedValue=((JList)evt.getSource()).getSelectedValue();
        if(selectedValue!=null)
        {
            wordTextField.setText(selectedValue.toString().trim());
            //meaningTextArea.setText(meaningFromBase.get(selectedValue));
        }
    }

    private void cleanBaseOfRepeatedWords()
    {
        if(cleanRepeatedWordsCheckBox.isSelected())
        {
            try
            {
                NodeList itemNodeList=baseElement.getElementsByTagName("item");
                int countItemNodeList=itemNodeList.getLength();
                for(int i=0;i<countItemNodeList-1;i++)
                {
                    Node nodeI=itemNodeList.item(i);
                    String textI=((Element)nodeI).getElementsByTagName("word").item(0).getTextContent().trim();
                    for(int j=i+1;j<countItemNodeList;j++)
                    {
                        Node nodeJ=itemNodeList.item(j);
                        String textJ=((Element)nodeJ).getElementsByTagName("word").item(0).getTextContent().trim();
                        if(textJ.equalsIgnoreCase(textI)||textJ.equals(""))
                        {
                            baseElement.removeChild(nodeJ);
                            --j;
                            itemNodeList=baseElement.getElementsByTagName("item");
                            countItemNodeList=itemNodeList.getLength();
                        }
                    }
                }

                rebuildBaseFileDocument(itemNodeList);

                DOMSource baseFileDOMSource=new DOMSource(baseFileDocument);
                baseFiletransformer.transform(baseFileDOMSource,baseFileStreamResult);
            }
            catch(Exception ex)
            {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE,null,ex);
                JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        exitButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        changeButton = new javax.swing.JButton();
        wordTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        hintsList = new javax.swing.JList();
        wordLabel = new javax.swing.JLabel();
        hintsLabel = new javax.swing.JLabel();
        meaningLabel = new javax.swing.JLabel();
        similarsLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        similarsList = new javax.swing.JList();
        deleteButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        meaningTextArea = new javax.swing.JTextArea();
        cleanRepeatedWordsCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        addButton.setText("Add");
        addButton.setEnabled(false);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        changeButton.setText("Change");
        changeButton.setEnabled(false);
        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });

        hintsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                hintsListMouseClicked(evt);
            }
        });
        hintsList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                hintsListKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(hintsList);

        wordLabel.setText("Word:");

        hintsLabel.setText("Hints:");

        meaningLabel.setText("Meaning:");

        similarsLabel.setText("Similars:");

        similarsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                similarsListMouseClicked(evt);
            }
        });
        similarsList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                similarsListKeyTyped(evt);
            }
        });
        jScrollPane3.setViewportView(similarsList);

        deleteButton.setText("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        meaningTextArea.setColumns(20);
        meaningTextArea.setRows(5);
        jScrollPane2.setViewportView(meaningTextArea);

        cleanRepeatedWordsCheckBox.setText("Before exit to check and clean the base of the repeated words");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                    .addComponent(cleanRepeatedWordsCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wordLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wordTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(exitButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 351, Short.MAX_VALUE)
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton))
                    .addComponent(hintsLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                    .addComponent(meaningLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                    .addComponent(similarsLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wordLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hintsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(meaningLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(similarsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cleanRepeatedWordsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitButton)
                    .addComponent(addButton)
                    .addComponent(changeButton)
                    .addComponent(deleteButton)
                    .addComponent(resetButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitButtonActionPerformed
    {//GEN-HEADEREND:event_exitButtonActionPerformed
        cleanBaseOfRepeatedWords();
        saveConfig();
        System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
    {//GEN-HEADEREND:event_addButtonActionPerformed
        addWord();
    }//GEN-LAST:event_addButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        cleanBaseOfRepeatedWords();
        saveConfig();
    }//GEN-LAST:event_formWindowClosing

    private void hintsListMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_hintsListMouseClicked
    {//GEN-HEADEREND:event_hintsListMouseClicked
        applySelectedItem(evt);
    }//GEN-LAST:event_hintsListMouseClicked

    private void hintsListKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_hintsListKeyTyped
    {//GEN-HEADEREND:event_hintsListKeyTyped
        applySelectedItem(evt);
    }//GEN-LAST:event_hintsListKeyTyped

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_resetButtonActionPerformed
    {//GEN-HEADEREND:event_resetButtonActionPerformed
        wordTextField.setText("");
        meaningTextArea.setText("");
    }//GEN-LAST:event_resetButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        actionOnWord("delete");
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_changeButtonActionPerformed
    {//GEN-HEADEREND:event_changeButtonActionPerformed
        actionOnWord("change");
    }//GEN-LAST:event_changeButtonActionPerformed

    private void similarsListMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_similarsListMouseClicked
    {//GEN-HEADEREND:event_similarsListMouseClicked
        applySelectedItem(evt);
    }//GEN-LAST:event_similarsListMouseClicked

    private void similarsListKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_similarsListKeyTyped
    {//GEN-HEADEREND:event_similarsListKeyTyped
        applySelectedItem(evt);
    }//GEN-LAST:event_similarsListKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton changeButton;
    private javax.swing.JCheckBox cleanRepeatedWordsCheckBox;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JLabel hintsLabel;
    private javax.swing.JList hintsList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel meaningLabel;
    private javax.swing.JTextArea meaningTextArea;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel similarsLabel;
    private javax.swing.JList similarsList;
    private javax.swing.JLabel wordLabel;
    private javax.swing.JTextField wordTextField;
    // End of variables declaration//GEN-END:variables
}
