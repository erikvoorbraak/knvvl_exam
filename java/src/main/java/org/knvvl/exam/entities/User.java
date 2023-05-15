package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;

@Entity
@Table(name="t_user")
@Cacheable @Cache(usage = READ_WRITE)
public class User
{
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    public Integer getId()
    {
        return id;
    }

    public User()
    {
    }

    public User(int id, String username)
    {
        this.id = id;
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public String getEncodedPassword()
    {
        return password;
    }

    public void setEncodedPassword(String encodedPassword)
    {
        this.password = encodedPassword;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Override
    public String toString()
    {
        return username;
    }
}
