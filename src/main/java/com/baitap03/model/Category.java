package com.baitap03.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "categories",
    schema = "dbo"
)
@NamedQuery(
    name = "Category.findAll",
    query = "SELECT c FROM Category c"
)
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryId")
    private int categoryid;


    @Column(
        name = "CategoryName",
        nullable = false,
        columnDefinition = "nvarchar(50)"
    )
    private String categoryname;


    @Column(
        name = "Images",
        nullable = true,
        columnDefinition = "nvarchar(500)"
    )
    private String images;


    @Column(name = "Status")
    private int status;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();


    // Constructor không tham số
    public Category() {
    }


    // Constructor đầy đủ
    public Category(
            int categoryid,
            String categoryname,
            String images,
            int status) {

        this.categoryid = categoryid;
        this.categoryname = categoryname;
        this.images = images;
        this.status = status;
    }


    // CategoryId

    public int getCategoryid() {

        return categoryid;
    }

    public void setCategoryid(int categoryid) {

        this.categoryid = categoryid;
    }


    // CategoryName

    public String getCategoryname() {

        return categoryname;
    }

    public void setCategoryname(String categoryname) {

        this.categoryname = categoryname;
    }


    // Images

    public String getImages() {

        return images;
    }

    public void setImages(String images) {

        this.images = images;
    }


    // Status

    public int getStatus() {

        return status;
    }

    public void setStatus(int status) {

        this.status = status;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
