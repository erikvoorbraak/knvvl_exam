<template>
  <main>
    <form v-on:submit.prevent="submit"> 
        <table>
            <tr><td>Naam:</td><td><input v-model="form.label"/></td></tr>
            <tr><td>Brevet:</td><td>
                <select v-model="form.certificate">
                    <option value="2">2</option>
                    <option value="3">3</option>
                </select>
            </td></tr>
            <tr><td>Taal</td><td>
                <select v-model="form.language">
                    <option value="nl">nl</option>
                    <option value="en">en</option>
                </select>
            </td></tr>
        </table>
        <button class="button is-primary">Maak examen</button>
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