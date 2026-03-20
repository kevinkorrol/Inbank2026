<script lang="ts">
	import { requestLoanDecision, type DecisionResponse } from '$lib/api/decision';

	let idCode = '';
	let loanAmount = '';
	let loanDuration = '';

	let loading = false;
	let errorMessage = '';
	let result: DecisionResponse | null = null;

	async function handleSubmit(event: SubmitEvent) {
		event.preventDefault();
		errorMessage = '';
		result = null;

		const parsedAmount = Number(loanAmount);
		const parsedDuration = Number(loanDuration);

		if (!idCode.trim()) {
			errorMessage = 'Personal ID code is required.';
			return;
		}

		if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
			errorMessage = 'Loan amount must be a positive number.';
			return;
		}

		if (!Number.isInteger(parsedDuration) || parsedDuration <= 0) {
			errorMessage = 'Loan duration must be a positive whole number.';
			return;
		}

		loading = true;

		try {
			result = await requestLoanDecision({
				idCode: idCode.trim(),
				loanAmount: parsedAmount,
				loanDuration: parsedDuration
			});
		} catch (error) {
			errorMessage = error instanceof Error ? error.message : 'Failed to get a decision.';
		} finally {
			loading = false;
		}
	}
</script>

<main class="container">
	<h1>Loan Decision</h1>

	<form on:submit={handleSubmit} class="decision-form">
		<label>
			Personal ID code
			<input type="text" bind:value={idCode} placeholder="e.g. 49002010965" required />
		</label>

		<label>
			Loan amount
			<input type="number" bind:value={loanAmount} min="1" step="1" required />
		</label>

		<label>
			Loan duration (months)
			<input type="number" bind:value={loanDuration} min="1" step="1" required />
		</label>

		<button type="submit" disabled={loading}>
			{#if loading}Checking...{:else}Get decision{/if}
		</button>
	</form>

	{#if errorMessage}
		<p class="error">{errorMessage}</p>
	{/if}

	{#if result}
		<section class="result" aria-live="polite">
			<h2>Decision result</h2>
			<p><strong>Decision:</strong> {result.decision}</p>
			<p><strong>Approved sum:</strong> {result.loanSum}</p>
		</section>
	{/if}
</main>

<style>
	.container {
		max-width: 36rem;
		margin: 2rem auto;
		padding: 1rem;
	}

	.decision-form {
		display: grid;
		gap: 1rem;
	}

	label {
		display: grid;
		gap: 0.4rem;
	}

	input,
	button {
		padding: 0.6rem;
		font: inherit;
	}

	button {
		cursor: pointer;
	}

	.error {
		color: #b00020;
	}

	.result {
		margin-top: 1.5rem;
		padding: 1rem;
		border: 1px solid #ccc;
		border-radius: 0.5rem;
	}
</style>
