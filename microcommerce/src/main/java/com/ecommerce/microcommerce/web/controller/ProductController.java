package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@Api("API pour les opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;

//    public ProductController(ProductDao productDao){
//        this.productDao = productDao;
//    }

    //Récupérer la liste des produits
    @GetMapping("/produits")
    public MappingJacksonValue listeProduits() {
        Iterable<Product> produits = productDao.findAll();

//        https://openclassrooms.com/fr/courses/4668056-construisez-des-microservices/7652183-renvoyez-les-bons-codes-et-filtrez-les-reponses
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }

    @ApiOperation(value = "Récupère un produit grâce à son ID à condition qu'il soit en stock")
    @GetMapping("/produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {
        Product produit = productDao.findById(id);
        if(produit==null) throw new ProduitIntrouvableException("Produit : " + id + " introuvable.");
        return produit;
    }

    @PostMapping(value = "/produits")
    public ResponseEntity<Product> ajouterProduit(@Valid @RequestBody Product product) {

        Product productAdded = productDao.save(product);
        if (Objects.isNull(productAdded)) {
            return ResponseEntity.noContent().build();
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "test/produits/{prixLimit}")
    public List<Product> testDeRequetes(@PathVariable int prixLimit)
    {
        return productDao.findByPrixGreaterThan(400);
    }

    @DeleteMapping (value = "/produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        productDao.deleteById(id);
    }

    @PutMapping (value = "/produits")
    public void updateProduit(@RequestBody Product product)
    {
        productDao.save(product);
    }
}
