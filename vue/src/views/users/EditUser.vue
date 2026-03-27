<template>
    <main>
      <form v-on:submit.prevent="submit"> 
      <table>
          <tr><td>Username</td><td><input v-model="form.username" readonly/></td></tr>
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
                  const userId = this.$route.params.id;
                  await axios.post(`/api/users/${userId}`, this.form)
                  this.$router.push('/users')
              } catch (error) {
                  if (error.response && error.response.data) {
                      if (typeof error.response.data === 'string') {
                          alert(error.response.data);
                      } else {
                          alert('Error updating user');
                      }
                  } else {
                      alert('Error updating user');
                  }
              }
          }
      },
      mounted() {
        document.title = "Gebruiker bewerken";
        const userId = this.$route.params.id;
        axios.get(`/api/users/${userId}`).then((response) => { 
            this.form = response.data;
            this.form.password = ''; // Clear password field
        });
    }
  }
  </script>
