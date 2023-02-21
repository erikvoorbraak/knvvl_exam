<script setup>
import { RouterLink, RouterView } from "vue-router";
</script>

<template>
  <table style="width: 100%;">
  <tr><td><nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/login">Login</RouterLink>
        <RouterLink to="/questions">Vragen</RouterLink>
        <RouterLink to="/topics">Vakken</RouterLink>
        <RouterLink to="/requirements">Exameneisen</RouterLink>
        <RouterLink to="/exams">Examens</RouterLink>
        <RouterLink to="/pictures">Afbeeldingen</RouterLink>
        <RouterLink to="/texts">Settings</RouterLink>
        <RouterLink to="/users">Gebruikers</RouterLink>
        <RouterLink to="/myaccount">Account</RouterLink>
      </nav></td></tr>

  <tr><td><RouterView /></td></tr>
</table>
</template>

<style>
header {
  line-height: 1.5;
  max-height: 100vh;
}
td {
  display: table-cell;
  vertical-align: top;
  color: #666 !important;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }

  textarea  
  {  
    font-family:"Segoe UI", Arial, sans-serif;  
    font-size: 12px;
  }
}
</style>
