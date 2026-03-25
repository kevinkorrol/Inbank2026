import { afterEach, describe, expect, it, vi } from 'vitest';

import { requestLoanDecision } from './decision';

afterEach(() => {
	vi.restoreAllMocks();
});

describe('requestLoanDecision', () => {
	it('sends mapped payload and returns positive decision', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({
				decision: 'POSITIVE',
				approvedAmount: 6000,
				approvedPeriod: 60,
				message: 'Loan approved'
			})
		});

		vi.stubGlobal('fetch', fetchMock);

		const result = await requestLoanDecision({
			idCode: '49002010976',
			loanAmount: 4000,
			loanPeriod: 24
		});

		expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/loan/decision', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				personalCode: '49002010976',
				loanAmount: 4000,
				loanPeriod: 24
			})
		});

		expect(result).toEqual({
			decision: 'POSITIVE',
			approvedAmount: 6000,
			approvedPeriod: 60,
			message: 'Loan approved'
		});
	});

	it('returns negative response contract when API sends negative decision', async () => {
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({
				ok: true,
				json: async () => ({
					decision: 'NEGATIVE',
					approvedAmount: 0,
					approvedPeriod: 0,
					message: 'This user has existing debt'
				})
			})
		);

		const result = await requestLoanDecision({
			idCode: '49002010965',
			loanAmount: 4000,
			loanPeriod: 24
		});

		expect(result).toEqual({
			decision: 'NEGATIVE',
			approvedAmount: 0,
			approvedPeriod: 0,
			message: 'This user has existing debt'
		});
	});

	it('throws a useful error when API returns non-OK status', async () => {
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({
				ok: false,
				status: 400
			})
		);

		await expect(
			requestLoanDecision({
				idCode: '49002010976',
				loanAmount: 4000,
				loanPeriod: 24
			})
		).rejects.toThrow('Decision request failed with status 400');
	});
});
