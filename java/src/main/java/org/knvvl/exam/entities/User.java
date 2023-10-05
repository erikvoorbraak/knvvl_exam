package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.IdEntity;
import org.knvvl.exam.meta.EntityField.EntityFieldString;
import org.knvvl.exam.meta.EntityFields;

@Entity
@Table(name="t_user")
@Cacheable @Cache(usage = READ_WRITE)
public class User implements IdEntity
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

    @Override
    public Integer getId()
    {
        return id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    public static EntityFields<User> getFields()
    {
        return new EntityFields<>(List.of(
            new EntityFieldString<>("username", User::getUsername, User::setUsername),
            new EntityFieldString<>("password", User::getEncodedPassword, User::setEncodedPassword),
            new EntityFieldString<>("email", User::getEmail, User::setEmail)));
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

    public void setUsername(String username)
    {
        this.username = username;
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
