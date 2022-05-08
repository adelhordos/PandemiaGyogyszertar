package com.example.gyogyszertar;
//egyes termékek leírása
public class ProductItem {
    private String id;
    private String name;
    private String itemtype;
    private String price;
    private float ratedInfo;
    private int imageResource;
    private int cartCounter; //számláljuk hogy hányszor rakták kosárba a terméket

    public ProductItem() {
    }

    public ProductItem(String name, String itemtype, String price, float ratedInfo, int imageResource, int cartCounter) {
        this.name = name;
        this.itemtype = itemtype;
        this.price = price;
        this.ratedInfo = ratedInfo;
        this.imageResource = imageResource;
        this.cartCounter=cartCounter;
    }

    public String getName() {
        return name;
    }

    public String getItemtype() {
        return itemtype;
    }

    public String getPrice() {
        return price;
    }

    public float getRatedInfo() {
        return ratedInfo;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getCartCounter() {
        return cartCounter;
    }
    public String _getId(){
        return id;
    }
    public void setId(String id){
        this.id=id;
    }
}
