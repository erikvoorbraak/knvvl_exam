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
      data() {
          return {
              form: {}
          }  
      },
      methods: {
          async submit() {
              try {
                  await axios.post('/api/users/me', this.form)
                  this.$router.push('/users')
              } catch (error) {
                  if (error.response) {
                    alert(error.response.data);
                  }
              }
          }
      },
      mounted() {
        document.title = "Account bewerken";
        axios.get('/api/users/me').then((response) => { this.form = response.data });
    }
  }
  </script>