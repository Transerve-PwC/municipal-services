
package org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="encryptedempcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="encryptedmonth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="encryptedyear" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "encryptedempcode",
    "encryptedmonth",
    "encryptedyear"
})
@XmlRootElement(name = "getPaySlip")
public class GetPaySlip {

    protected String encryptedempcode;
    protected String encryptedmonth;
    protected String encryptedyear;

    /**
     * Gets the value of the encryptedempcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncryptedempcode() {
        return encryptedempcode;
    }

    /**
     * Sets the value of the encryptedempcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncryptedempcode(String value) {
        this.encryptedempcode = value;
    }

    /**
     * Gets the value of the encryptedmonth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncryptedmonth() {
        return encryptedmonth;
    }

    /**
     * Sets the value of the encryptedmonth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncryptedmonth(String value) {
        this.encryptedmonth = value;
    }

    /**
     * Gets the value of the encryptedyear property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncryptedyear() {
        return encryptedyear;
    }

    /**
     * Sets the value of the encryptedyear property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncryptedyear(String value) {
        this.encryptedyear = value;
    }

}
