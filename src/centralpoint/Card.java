/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package centralpoint;

/**
 *
 * @author field
 */
public class Card {
    
    public String bankName;
    public String cardNum;
    public String cardHolder;
    public String branchCode;
    public String expiryDate;
    
    
    public Card() {
        this.bankName = "";
        this.cardNum = "";
        this.cardHolder = "";
        this.branchCode = "";
        this.expiryDate = "";
    }
    
    public Card(String bankName, String cardNum, String cardHolder, String branchCode, String expiryDate) {
        this.bankName = bankName;
        this.cardNum = cardNum;
        this.cardHolder = cardHolder;
        this.branchCode = branchCode;
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return "Card{" + "bankName=" + bankName + ", cardNum=" + cardNum + ", cardHolder=" + cardHolder + ", branchCode=" + branchCode + ", expiryDate=" + expiryDate + '}'+"\n";
    }
    
    
    
}
