<template>
    <main>
      <form v-on:submit.prevent="submit"> 
      <table>
          <tr><td>Username</td><td><input v-model="form.username"/></td></tr>
          <tr><td>Password</td><td><input v-model="form.password" type="password" /></td></tr>
          <tr><td>Email</td><td><input v-model="form.email"/></td></tr>
        </table>
          <button class="button is-primary">Submit</button>
      </form>
    </main>
  </template>
  <script>
  import axios from 'axios'
  export default {
      setup() {
          document.title = "Nieuwe gebruiker";
      },
      data() {
          return {
              form: {}
          }  
      },
      methods: {
          async submit() {
              try {
                  await axios.post('/api/users', this.form)
                  this.$router.push('/users')
              } catch (error) {
                  if (error.response) {
                  alert(error.response.data);
                  }
              }
          }
      }
  }
  </script>