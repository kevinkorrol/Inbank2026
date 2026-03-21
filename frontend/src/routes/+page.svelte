<script lang="ts">
	import { requestLoanDecision, type DecisionResponse } from '$lib/api/decision';

	const AMOUNT_MIN = 2000;
	const AMOUNT_MAX = 10000;
	const PERIOD_MIN = 12;
	const PERIOD_MAX = 60;

	let idCode = $state('');
	let personalCodeTouched = $state(false);
	let amountTouched = $state(false);
	let periodTouched = $state(false);
	let loanAmount = $state(4000);
	let loanPeriod = $state(24);
	let loanAmountInput = $state('4000');
	let loanPeriodInput = $state('24');

	let loading = $state(false);
	let errorMessage = $state('');
	let result = $state<DecisionResponse | null>(null);

	function isAmountValid(value: number): boolean {
		return Number.isFinite(value) && value >= AMOUNT_MIN && value <= AMOUNT_MAX;
	}

	function isPeriodValid(value: number): boolean {
		return Number.isInteger(value) && value >= PERIOD_MIN && value <= PERIOD_MAX;
	}

	function isPersonalCodeValid(value: string): boolean {
		return /^\d+$/.test(value.trim());
	}

	function handleAmountInput(event: Event) {
		const raw = (event.currentTarget as HTMLInputElement).value;
		loanAmountInput = raw;

		const parsed = Number(raw);
		if (isAmountValid(parsed)) {
			loanAmount = parsed;
		}
	}

	function handleAmountSliderInput(event: Event) {
		const value = Number((event.currentTarget as HTMLInputElement).value);
		loanAmount = value;
		loanAmountInput = String(value);
	}

	function handlePeriodInput(event: Event) {
		const raw = (event.currentTarget as HTMLInputElement).value;
		loanPeriodInput = raw;

		const parsed = Number(raw);
		if (isPeriodValid(parsed)) {
			loanPeriod = parsed;
		}
	}

	function handlePeriodSliderInput(event: Event) {
		const value = Number((event.currentTarget as HTMLInputElement).value);
		loanPeriod = value;
		loanPeriodInput = String(value);
	}

	function handleAmountBlur() {
		amountTouched = true;
	}

	function handlePeriodBlur() {
		periodTouched = true;
	}

	function handlePersonalCodeBlur() {
		personalCodeTouched = true;
	}

	const personalCodeError = $derived.by(() => {
		if (!personalCodeTouched) {
			return '';
		}

		const trimmed = idCode.trim();

		if (!trimmed) {
			return 'Personal code is required';
		}

		if (!isPersonalCodeValid(trimmed)) {
			return 'Personal code can only contain numbers';
		}

		return '';
	});

	const amountError = $derived.by(() => {
		if (!amountTouched) {
			return '';
		}

		if (!loanAmountInput.trim()) {
			return 'Loan amount is required';
		}

		const parsed = Number(loanAmountInput);
		if (!isAmountValid(parsed)) {
			return `Loan amount must be between ${AMOUNT_MIN} and ${AMOUNT_MAX} euros`;
		}

		return '';
	});

	const periodError = $derived.by(() => {
		if (!periodTouched) {
			return '';
		}

		if (!loanPeriodInput.trim()) {
			return 'Loan period is required';
		}

		const parsed = Number(loanPeriodInput);
		if (!isPeriodValid(parsed)) {
			return `Loan period must be between ${PERIOD_MIN} and ${PERIOD_MAX} months`;
		}

		return '';
	});

	const amountFill = $derived(
		`${((loanAmount - AMOUNT_MIN) / (AMOUNT_MAX - AMOUNT_MIN)) * 100}%`
	);

	const periodFill = $derived(
		`${((loanPeriod - PERIOD_MIN) / (PERIOD_MAX - PERIOD_MIN)) * 100}%`
	);

	const canSubmit = $derived(
		!loading &&
			personalCodeError === '' &&
			amountError === '' &&
			periodError === ''
	);

	async function handleSubmit(event: SubmitEvent) {
		console.log('Submitting loan form');
		event.preventDefault();
		errorMessage = '';
		result = null;
		personalCodeTouched = true;
		amountTouched = true;
		periodTouched = true;

		if (personalCodeError) {
			errorMessage = personalCodeError;
			return;
		}

		if (amountError) {
			errorMessage = amountError;
			return;
		}

		if (periodError) {
			errorMessage = periodError;
			return;
		}

		loading = true;

		try {
			result = await requestLoanDecision({
				idCode: idCode.trim(),
				loanAmount,
				loanPeriod
			});
		} catch (error) {
			errorMessage =
				error instanceof Error
					? error.message
					: 'Failed to get a decision. Check that backend is running on localhost:8080.';
		} finally {
			loading = false;
		}
	}
</script>

<main class="container">
	<header class="page-header">
		<div class="brand">
			<img src="/inbank.png" alt="Inbank" class="brand-logo" />
			<div>
				<p class="brand-name">Inbank</p>
				<h1>Loan Application</h1>
			</div>
		</div>
	</header>

	<form onsubmit={handleSubmit} class="decision-form">
		<label>
			Personal code
			<input
				type="text"
				bind:value={idCode}
				placeholder="Enter personal code"
				autocomplete="off"
				inputmode="numeric"
				onblur={handlePersonalCodeBlur}
			/>
			{#if personalCodeError}
				<p class="field-error">{personalCodeError}</p>
			{/if}
		</label>

		<div class="field-block">
			<div class="field-head">
				<label for="loan-amount">Loan amount</label>
				<div class="value-input">
					<span class="prefix">EUR</span>
					<input
						id="loan-amount"
						type="number"
						value={loanAmountInput}
						oninput={handleAmountInput}
						onblur={handleAmountBlur}
						min={AMOUNT_MIN}
						max={AMOUNT_MAX}
						step="1"
					/>
				</div>
			</div>
			{#if amountError}
				<p class="field-error">{amountError}</p>
			{/if}
			<input
				type="range"
				value={loanAmount}
				oninput={handleAmountSliderInput}
				style={`--fill:${amountFill}`}
				min={AMOUNT_MIN}
				max={AMOUNT_MAX}
				step="1"
				aria-label="Loan amount"
			/>
			<div class="range-labels">
				<span>EUR {AMOUNT_MIN}</span>
				<span>EUR {AMOUNT_MAX}</span>
			</div>
		</div>

		<div class="field-block">
			<div class="field-head">
				<label for="loan-period">Loan period</label>
				<div class="value-input">
					<input
						id="loan-period"
						type="number"
						value={loanPeriodInput}
						oninput={handlePeriodInput}
						onblur={handlePeriodBlur}
						min={PERIOD_MIN}
						max={PERIOD_MAX}
						step="1"
					/>
					<span class="suffix">months</span>
				</div>
			</div>
			{#if periodError}
				<p class="field-error">{periodError}</p>
			{/if}
			<input
				type="range"
				value={loanPeriod}
				oninput={handlePeriodSliderInput}
				style={`--fill:${periodFill}`}
				min={PERIOD_MIN}
				max={PERIOD_MAX}
				step="1"
				aria-label="Loan period"
			/>
			<div class="range-labels">
				<span>{PERIOD_MIN} months</span>
				<span>{PERIOD_MAX} months</span>
			</div>
		</div>

		<button type="submit" disabled={!canSubmit}>
			{#if loading}Calculating...{:else}Calculate{/if}
		</button>
	</form>

	{#if result}
		<section class="result" aria-live="polite">
			<p class="decision-line">
				Decision result:
				<span
					class:decision-positive={result.decision === 'POSITIVE'}
					class:decision-negative={result.decision !== 'POSITIVE'}
				>
					{result.decision === 'POSITIVE' ? 'Approved' : 'Not approved'}
				</span>
			</p>
			<p><strong>Message:</strong> {result.message}</p>
			{#if result.decision === 'POSITIVE'}
				<p><strong>Approved amount:</strong> EUR {result.approvedAmount}</p>
				<p><strong>Approved period:</strong> {result.approvedPeriod} months</p>
			{/if}
		</section>
	{/if}
</main>

<style>
	.container {
		max-width: 40rem;
		margin: 2rem auto;
		padding: 1.5rem;
		border: 1px solid #d9deea;
		border-radius: 0.85rem;
		background: #ffffff;
		box-shadow: 0 6px 18px rgb(11 37 82 / 8%);
	}

	.decision-form {
		display: grid;
		gap: 1.25rem;
	}

	label {
		display: grid;
		gap: 0.4rem;
		font-weight: 600;
	}

	input,
	button {
		padding: 0.6rem;
		font: inherit;
	}

	input[type='number'] {
		border: 1px solid #c8cfde;
		border-radius: 0.5rem;
	}

	input[type='text'] {
		border: 1px solid #c9bbe9;
		border-radius: 0.6rem;
		background: #faf7ff;
		font-weight: 600;
		padding: 0.75rem;
	}

	.field-error {
		margin: 0.15rem 0 0;
		font-size: 0.9rem;
		font-weight: 600;
		color: #8a2d5d;
	}

	input[type='text']:focus {
		outline: 2px solid #c3b2ef;
		outline-offset: 1px;
	}

	.page-header {
		margin-bottom: 1.25rem;
	}

	.brand {
		display: flex;
		align-items: center;
		gap: 0.9rem;
		padding-bottom: 0.9rem;
		border-bottom: 1px solid #e3d8f5;
	}

	.brand-logo {
		width: 2.4rem;
		height: 2.4rem;
		object-fit: contain;
		border-radius: 0.4rem;
	}

	.brand-name {
		margin: 0;
		font-size: 0.9rem;
		letter-spacing: 0.06em;
		text-transform: uppercase;
		color: #7f66be;
		font-weight: 700;
	}

	h1 {
		margin: 0.15rem 0 0;
	}

	.field-block {
		display: grid;
		gap: 0.6rem;
	}

	.field-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 0.8rem;
	}

	.value-input {
		display: flex;
		align-items: center;
		gap: 0.4rem;
	}

	.value-input input {
		width: 6rem;
		text-align: right;
	}

	.prefix,
	.suffix {
		color: #4f5d7a;
		font-size: 0.95rem;
	}

	input[type='range'] {
		-webkit-appearance: none;
		appearance: none;
		background: transparent;
		height: 1.6rem;
		padding: 0;
	}

	.range-labels {
		display: flex;
		justify-content: space-between;
		font-size: 0.92rem;
		color: #546482;
	}

	button {
		cursor: pointer;
		border: none;
		border-radius: 0.55rem;
		background: #8f76d8;
		color: #fff;
		font-weight: 700;
		transition: background-color 0.18s ease;
	}

	button:disabled {
		background: #d4c9ee;
		cursor: not-allowed;
	}

	button:not(:disabled):hover {
		background: #7355bf;
	}

	input[type='range']::-webkit-slider-thumb {
		-webkit-appearance: none;
		appearance: none;
		width: 1.7rem;
		height: 1.7rem;
		border-radius: 50%;
		background: #a791e8;
		border: 2px solid #8f74d8;
		cursor: pointer;
		margin-top: -0.6rem;
	}

	input[type='range']::-webkit-slider-runnable-track {
		height: 0.5rem;
		border-radius: 999px;
		background: linear-gradient(to right, #5b2b81 0 var(--fill), #d8c9f2 var(--fill) 100%);
	}

	input[type='range']::-moz-range-thumb {
		width: 1.7rem;
		height: 1.7rem;
		border-radius: 50%;
		background: #a791e8;
		border: 2px solid #8f74d8;
		cursor: pointer;
	}

	input[type='range']::-moz-range-track {
		height: 0.5rem;
		border-radius: 999px;
		background: #d8c9f2;
	}

	input[type='range']::-moz-range-progress {
		height: 0.5rem;
		border-radius: 999px;
		background: #5b2b81;
	}

	.result {
		margin-top: 1.5rem;
		padding: 1rem;
		border: 1px solid #d5c7ef;
		border-radius: 0.5rem;
		background: #f3edff;
	}

	.decision-line {
		margin: 0 0 0.55rem;
		font-size: 1.35rem;
		font-weight: 700;
		line-height: 1.2;
		color: #3f2e67;
	}

	.decision-line span {
		color: #5a3f95;
	}

	.decision-line span.decision-positive {
		color: #2f8f4d;
	}

	.decision-line span.decision-negative {
		color: #c24a5a;
	}

	@media (max-width: 640px) {
		.container {
			margin: 1rem;
			padding: 1rem;
		}

		.field-head {
			flex-direction: column;
			align-items: flex-start;
		}
	}
</style>
