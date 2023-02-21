<template>
  <main>
    <form v-on:submit.prevent="submit"> 
        Naam: <input v-model="form.label"/><br/>
        Brevet: <select v-model="form.certificate">
            <option value="2">2</option>
            <option value="3">3</option>
        </select><br/>
        <button class="button is-primary">Submit</button>
    </form>
  </main>
</template>
<script>
import axios from 'axios'
export default {
    setup() {
        document.title = "Nieuw examen";
    },
    data() {
        return {
            form: {}
        }  
    },
    methods: {
        async submit() {
            try {
                await axios.post('/api/exams', this.form)
                this.$router.push('/exams')
            } catch (error) {
                if (error.response) {
                alert(error.response.data);
                }
            }
        }
    }
}
</script>