<script setup>
import TableExams from "../components/TableExams.vue";
document.title = "Examens";
</script>
<template>
  <main>
    <RouterLink to="/newexam">Nieuw examen</RouterLink>
    <TableExams/>
  </main>
</template>
