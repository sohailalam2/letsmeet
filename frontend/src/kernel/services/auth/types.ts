export interface UserAuthResponseDTO {
  user_id: string
  access_token: string
  creation_timestamp: number
}

export interface UserPayload {
  sub: string;
  aud: string;
  nbf: number;
  name: string;
  iss: string;
  exp: number;
  iat: number;
  email: string;
  picture: string;
}
