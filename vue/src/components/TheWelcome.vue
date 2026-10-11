<script setup>
import WelcomeItem from './WelcomeItem.vue'
import DocumentationIcon from './icons/IconDocumentation.vue'
import ToolingIcon from './icons/IconTooling.vue'
import EcosystemIcon from './icons/IconEcosystem.vue'
import CommunityIcon from './icons/IconCommunity.vue'
import SupportIcon from './icons/IconSupport.vue'
</script>

<template>
  <WelcomeItem>
    <template #icon><DocumentationIcon /></template>
    <template #heading>Documentation</template>

    This tool is built to manage multiple-choice questions in order to create a 100-question theoretical exam
    about Paragliding. When the candidate passes this exam, and also has the necessary practical requirements,
    then he or she can apply for a Dutch license as "Brevet 2" or "Brevet 3".
  </WelcomeItem>

  <WelcomeItem>
    <template #icon><ToolingIcon /></template>
    <template #heading>Tooling</template>

    See this page for some admin-only actions:
    <a href="/api/admin" target="_blank">/api/admin</a>.
  </WelcomeItem>

  <WelcomeItem>
    <template #icon>\<CommunityIcon /></template>
    <template #heading>Community</template>

    Created by Erik Voorbraak in February 2023.<br/>
    For more information about taking the exam, go to
    <a href="https://www.brevet.aero/paragliding">Kei website</a>.
    For information about paragliding in the Netherlands, visit
    <a href="https://www.knvvl.nl/paragliding">KNVvL website</a>.
    You should also find relevant contact information on these sites.
  </WelcomeItem>

</template>
