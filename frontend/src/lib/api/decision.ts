export type DecisionRequest = {
	idCode: string;
	loanAmount: number;
	loanDuration: number;
};

export type DecisionResponse = {
	decision: string;
	loanSum: number;
};

export async function requestLoanDecision(payload: DecisionRequest): Promise<DecisionResponse> {
	const response = await fetch('/api/decision', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(payload)
	});

	if (!response.ok) {
		throw new Error(`Decision request failed with status ${response.status}`);
	}

	const data = (await response.json()) as Partial<DecisionResponse>;

	if (typeof data.decision !== 'string' || typeof data.loanSum !== 'number') {
		throw new Error('Invalid response from decision API');
	}

	return {
		decision: data.decision,
		loanSum: data.loanSum
	};
}
