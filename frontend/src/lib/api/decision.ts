export type DecisionRequest = {
	idCode: string;
	loanAmount: number;
	loanPeriod: number;
};

export type Decision = 'POSITIVE' | 'NEGATIVE';

export type DecisionResponse = {
	decision: Decision;
	approvedAmount: number;
	approvedPeriod: number;
	message: string;
};

const BACKEND_DECISION_URL = 'http://localhost:8080/api/loan/decision';

type BackendDecisionResponse = {
	decision?: Decision;
	approvedAmount?: number;
	approvedPeriod?: number;
	message?: string | null;
};

export async function requestLoanDecision(payload: DecisionRequest): Promise<DecisionResponse> {
	console.log('requestLoanDecision called with payload');
	const response = await fetch(BACKEND_DECISION_URL, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			personalCode: payload.idCode,
			loanAmount: payload.loanAmount,
			loanPeriod: payload.loanPeriod
		})
	});

	console.log('Received decision response');
	if (!response.ok) {
		throw new Error(`Decision request failed with status ${response.status}`);
	}

	const data = (await response.json()) as BackendDecisionResponse;


	if (typeof data.message === 'string' && data.message.trim() && data.decision === 'NEGATIVE') {
		console.log('Returning negative decision');
		return {
			decision: data.decision,
			approvedAmount: typeof data.approvedAmount === 'number' ? data.approvedAmount : 0,
			approvedPeriod: typeof data.approvedPeriod === 'number' ? data.approvedPeriod : 0,
			message: data.message
		};
	}

	if (
		(data.decision !== 'POSITIVE' && data.decision !== 'NEGATIVE') ||
		typeof data.approvedAmount !== 'number' ||
		typeof data.approvedPeriod !== 'number' ||
		typeof data.message !== 'string'
	) {
		throw new Error('Invalid response from decision API');
	}

	console.log('Returning decision response:');
	return {
		decision: data.decision,
		approvedAmount: data.approvedAmount,
		approvedPeriod: data.approvedPeriod,
		message: data.message
	};
}
