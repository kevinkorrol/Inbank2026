<script lang="ts">
	import './layout.css';

	let { children } = $props();
</script>

<svelte:head>
	<link rel="icon" type="image/png" href="/inbank.png" />
	<link rel="shortcut icon" type="image/png" href="/inbank.png" />
</svelte:head>
{@render children()}
